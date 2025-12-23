package com.soundspace.service;

import com.soundspace.config.ApplicationConfigProperties;
import com.soundspace.dto.PlaylistDto;
import com.soundspace.dto.PlaylistSongViewDto;
import com.soundspace.dto.ProcessedImage;
import com.soundspace.dto.request.PlaylistCreateRequest;
import com.soundspace.dto.request.PlaylistUpdateRequest;
import com.soundspace.entity.*;
import com.soundspace.exception.AccessDeniedException;
import com.soundspace.exception.StorageException;
import com.soundspace.repository.PlaylistEntryRepository;
import com.soundspace.repository.PlaylistRepository;
import com.soundspace.repository.SongRepository;
import com.soundspace.repository.StorageKeyRepository;
import com.soundspace.service.song.SongCoreService;
import com.soundspace.service.storage.ImageService;
import com.soundspace.service.storage.StorageService;
import com.soundspace.service.user.AppUserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class PlaylistService {
    private final PlaylistRepository playlistRepository;
    private final AppUserService appUserService;
    private final StorageService storageService;
    private final ImageService imageService;
    private final StorageKeyRepository storageKeyRepository;
    private final SongCoreService songCoreService;
    private final SongRepository songRepository;
    private final PlaylistEntryRepository playlistEntryRepository;
    private final ApplicationConfigProperties.MediaConfig.CoverConfig coverConfig;

    @Cacheable(
            value = "all-playlists",
            key = "(#userDetails != null ? #userDetails.username : 'anonymous') + '_' + #pageable"
    )
    public Page<PlaylistDto> getAllPlaylists(UserDetails userDetails, Pageable pageable) {

        if (userDetails != null) {
            Long loggedInUserId = appUserService.getUserByEmail(userDetails.getUsername()).getId();
            return playlistRepository.findAllPublicOrOwnedByUser(loggedInUserId, pageable)
                    .map(PlaylistDto::toDto);

        } else return playlistRepository.findAllPublic(pageable).map(PlaylistDto::toDto);
    }


    @Transactional(readOnly = true)
    public List<PlaylistDto> getAllByUserId(Long userId, UserDetails userDetails) {
        List<Playlist> playlists;

        boolean isOwner = false;
        // jezeli jest null (niezalogowany) to tak samo jak dla zalogowanego nie-ownera
        if (userDetails != null) {
            Long loggedInUserId = appUserService.getUserByEmail(userDetails.getUsername()).getId();
            isOwner = userId.equals(loggedInUserId);
        }

        if (isOwner) {
            playlists = playlistRepository.getAllByCreatorId(userId);
        } else {
            playlists = playlistRepository.getAllPublicByCreatorId(userId);
        }

        return playlists.stream().map(PlaylistDto::toDto).toList();
    }

    @Cacheable(value = "playlist", key = "#playlistId")
    public PlaylistDto getPlaylist(Long playlistId, UserDetails userDetails) {
        Playlist playlist = playlistRepository.findById(playlistId).orElseThrow();
        ensureUserCanView(playlist, userDetails);
        return PlaylistDto.toDto(playlist);
    }

    public List<PlaylistSongViewDto> getSongs(Long playlistId, UserDetails userDetails) {
        ensureUserCanView(playlistRepository.findById(playlistId).orElseThrow(), userDetails);

        return playlistEntryRepository.findAllSongsInPlaylist(playlistId)
                .stream().map(PlaylistSongViewDto::toDto).toList();
    }

    @Transactional
    public PlaylistDto create(PlaylistCreateRequest request, UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        if (userEmail == null) throw new AccessDeniedException("User is not logged in");

        AppUser creator = appUserService.getUserByEmail(userEmail);

        Path tmpCoverPath = null;
        StorageKey coverStorageKeyEntity = null;

        try {
            MultipartFile coverFile = request.coverFile();
            if (coverFile == null || coverFile.isEmpty()) {
                coverStorageKeyEntity = storageKeyRepository.getReferenceById(coverConfig.defaultCoverId());

            } else {
                // resize, convert i zapis cover image do temp file
                tmpCoverPath = processCoverAndSaveToTemp(coverFile);

                // docelowy zapis cover image
                coverStorageKeyEntity = processAndSaveCoverFile(tmpCoverPath, creator);
            }

            Playlist playlist = new Playlist();
            playlist.setTitle(request.title());
            playlist.setCreator(creator);
            playlist.setPubliclyVisible(request.publiclyVisible());
            playlist.setCreatedAt(Instant.now());
            playlist.setUpdatedAt(Instant.now());
            playlist.setCoverStorageKey(coverStorageKeyEntity);

            Playlist savedPlaylist = playlistRepository.save(playlist);

            log.info("Utworzono playlistę id={} dla usera={}", savedPlaylist.getId(), creator.getLogin());

            return PlaylistDto.toDto(savedPlaylist);

        } catch (Exception e) {
            log.error("Błąd podczas tworzenia playlisty", e);

            // rollback
            rollbackStorage(coverStorageKeyEntity);

            if (e instanceof IOException) {
                throw new StorageException("Błąd I/O podczas przetwarzania okładki playlisty", e);
            }
            throw (RuntimeException) e;

        } finally {
            tryDeleteTempFile(tmpCoverPath);
        }
    }

    @Transactional
    public PlaylistSongViewDto addSong(Long playlistId, Long songId, UserDetails userDetails) {

        String userEmail = userDetails.getUsername();
        if (userEmail == null) throw new AccessDeniedException("Użytkownik nie jest zalogowany");

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono playlisty o id: " + playlistId));
        // todo zrobic custom exception jakis

        AppUser requestingUser = appUserService.getUserByEmail(userEmail);

        if (!playlist.getCreator().getId().equals(requestingUser.getId())) {
            throw new AccessDeniedException("Brak uprawnień do edycji playlisty");
        }

        if (playlistEntryRepository.existsBySongIdAndPlaylistId(songId, playlistId))
            throw new IllegalArgumentException("Piosenka już istnieje w playlist'cie");

        // rzuci wyjatek jak piosenka nie istnieje, wiec nie potrzeba ponownej walidacji
        Song song = songCoreService.getSongById(songId);


        // jezeli song nie jest publiczny i:
        // - requestujacy nie jest jej autorem -> brak dostepu
        // - piosenka jest w prywatnym albumie, a playlista jest publiczna -> throw
        // - requestujacy jest jej autorem i playlista jest publiczna -> zmiana prywatnosci songa na publiczny
        if (!song.getPubliclyVisible()) {

            if (!requestingUser.getId().equals(song.getAuthor().getId())) {
                throw new AccessDeniedException("Brak uprawnień do piosenki");

            } else if (playlist.getPubliclyVisible()) {
                if (song.getAlbum() != null)
                    throw new IllegalArgumentException("Piosenka należy do prywatnego albumu. Nie może istnieć w publicznej playlist'cie");

                song.setPubliclyVisible(true);
                songRepository.save(song);
                log.info("Zmieniono prywatnosc piosenki (id: {}) na publiczny po dodaniu do publicznej playlisty (id: {})",
                        song.getId(), playlist.getId());
            }

        }

        PlaylistEntry addedSong = playlist.addSong(song); // CascadeType.ALL, wiec przy zapisie playlisty PlaylistEntry tez sie zapisze

        playlist.setUpdatedAt(Instant.now());

        Playlist savedPlaylist = playlistRepository.save(playlist);

        log.info("Dodano piosenkę id={} do playlisty id={} (nowa pozycja: {})",
                song.getId(), playlist.getId(), savedPlaylist.getSongs().size() - 1);

        return PlaylistSongViewDto.toDto(addedSong);
    }

    @Transactional
    public void removeSong(Long playlistId, Long songId, UserDetails userDetails) {
        Playlist playlist = playlistRepository.findById(playlistId).orElseThrow();
        ensureUserIsOwnerOrAdmin(playlist, userDetails);
        playlistEntryRepository.deleteBySongIdAndPlaylistId(songId, playlistId);
        playlistEntryRepository.renumberPlaylist(playlistId);
    }

    @Transactional
    public void delete(Long playlistId, UserDetails userDetails) {
        Playlist playlist = playlistRepository.findById(playlistId).orElseThrow();
        ensureUserCanView(playlist, userDetails);

        playlistEntryRepository.deleteAllByPlaylistId(playlistId);

        StorageKey coverKey = playlist.getCoverStorageKey();
        playlistRepository.delete(playlist);
        playlistRepository.flush();

        try {
            if (coverKey != null && !coverKey.getId().equals(coverConfig.defaultCoverId()) && coverKey.getKeyStr() != null && !coverKey.getKeyStr().isBlank()) {
                try {
                    storageService.delete(coverKey.getKeyStr());
                } catch (Exception ex) {
                    log.warn("Nie udało się usunąć pliku cover z storage (playlist cover) storage key id={}", coverKey.getKeyStr(), ex);
                    throw ex;
                }
                try {
                    storageKeyRepository.deleteById(coverKey.getId());
                } catch (Exception ex) {
                    log.warn("Nie udało się usunąć rekordu storage_keys (playlist cover) storagekey id={}: {}", coverKey.getId(), ex.getMessage());
                }
            }

        } catch (AccessDeniedException e) {
            log.info(e.getMessage());
            throw e;
        } catch (Exception e) {
            log.info("Błąd podczas usuwania pliku/storage: {}", e.getMessage());
            throw new StorageException(e.getMessage());
        }

    }

    @Transactional
    public PlaylistDto update(Long playlistId, PlaylistUpdateRequest request, UserDetails userDetails) { // @AuthenticationPrincipal userDetails jest NotNull
        Playlist updatedPlaylist = playlistRepository.findById(playlistId).orElseThrow(); //narazie wszytkie pola takie same, a pozniej beda zmieniane zeby zapisac po update

        AppUser user = appUserService.getUserByEmail(userDetails.getUsername());
        if (!updatedPlaylist.getCreator().getId().equals(user.getId()))
            throw new AccessDeniedException("Brak dostępu do edycji piosenki");

        MultipartFile coverFile = request.coverFile();
        StorageKey storageKeyToDelete = null;
        if (coverFile != null && !coverFile.isEmpty()) {
            storageKeyToDelete = updatedPlaylist.getCoverStorageKey();
            updatedPlaylist.setCoverStorageKey(imageService.processAndSaveNewImage(
                    coverFile,
                    user,
                    coverConfig.width(),
                    coverConfig.height(),
                    coverConfig.quality(),
                    coverConfig.targetExtension(),
                    coverConfig.playlistDirectory(),
                    "cover"
            ));
        }

        if (request.title() != null) {
            updatedPlaylist.setTitle(request.title());
        }

        if (request.publiclyVisible() != null) {
            updatedPlaylist.setPubliclyVisible(request.publiclyVisible());
        }

        playlistRepository.save(updatedPlaylist);
        if (storageKeyToDelete != null) {
            imageService.cleanUpOldImage(storageKeyToDelete, "cover");
        }
        return PlaylistDto.toDto(updatedPlaylist);
    }

    @Transactional
    public PlaylistSongViewDto changeSongPosition(Long playlistId, Long songId,
                                                  Integer position,
                                                  UserDetails userDetails) {
        Playlist playlist = playlistRepository.findById(playlistId).orElseThrow();
        ensureUserIsOwnerOrAdmin(playlist, userDetails);

        PlaylistEntry playlistEntry = playlistEntryRepository.findBySongIdAndPlaylistId(songId, playlistId);

        playlistEntryRepository.updateSongPosition(playlistId, songId, playlistEntry.getPosition(), position);
        return PlaylistSongViewDto.toDto(playlistEntryRepository.findBySongIdAndPlaylistId(songId, playlistId));
    }


    // /////////////////////////////////// HELPERY ////////////////////////////////////  //

    private void ensureUserCanView(Playlist playlist, UserDetails userDetails) {
        if (!canView(playlist, userDetails)) {
            throw new AccessDeniedException("Brak dostępu do playlisty.");
        }
    }

    private boolean canView(Playlist playlist, UserDetails userDetails) {
        if (playlist.getPubliclyVisible()) {
            return true;
        }

        if (userDetails == null) {
            return false;
        }

        Long requestingUserId = appUserService.getUserByEmail(userDetails.getUsername()).getId();

        return requestingUserId.equals(playlist.getCreator().getId());
    }

    private void ensureUserIsOwnerOrAdmin(Playlist playlist, UserDetails userDetails) {
        AppUser requester = null;

        boolean isAdmin = false;
        if(userDetails != null) {
            requester = appUserService.getUserByEmail(userDetails.getUsername());
            isAdmin = requester.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }

        if (requester != null && !(requester.getId().equals(playlist.getCreator().getId()) || isAdmin))
            throw new AccessDeniedException("Brak dostępu do playlisty.");
    }

    private Path processCoverAndSaveToTemp(MultipartFile coverFile) throws IOException {
        ProcessedImage processedCover = imageService.resizeImageAndConvert(
                coverFile,
                coverConfig.width(),
                coverConfig.height(),
                coverConfig.targetExtension(),
                coverConfig.quality()
        );

        Path tmpCoverPath = Files.createTempFile("playlist-cover-", "." + coverConfig.targetExtension());
        Files.write(tmpCoverPath, processedCover.bytes());
        return tmpCoverPath;
    }

    //  zapis pliku z temp file, utworzenie i zapisanie opdpowiadajacej plikowi encji StorageKey
    private StorageKey processAndSaveCoverFile(Path tmpCoverPath, AppUser appUser) throws IOException {
        long coverFileSize = Files.size(tmpCoverPath);
        String mimeType = "image/" + coverConfig.targetExtension();

        String coverStorageKeyString = storageService.saveFromPath(
                tmpCoverPath,
                appUser.getId(),
                coverConfig.targetExtension(),
                coverConfig.playlistDirectory()
        );
        log.info("Zapisano okładkę playlisty: key={}", coverStorageKeyString);

        StorageKey storageKeyEntity = new StorageKey();
        storageKeyEntity.setKeyStr(coverStorageKeyString);
        storageKeyEntity.setMimeType(mimeType);
        storageKeyEntity.setSizeBytes(coverFileSize);

        return storageKeyRepository.save(storageKeyEntity);
    }

    private void rollbackStorage(StorageKey keyEntity) {
        if (keyEntity == null) return;
        try {
            storageService.delete(keyEntity.getKeyStr());
        } catch (Exception ex) {
            log.warn("Nie udało się usunąć pliku ze storage (rollback): {}", keyEntity.getKeyStr(), ex);
        }
        try {
            storageKeyRepository.delete(keyEntity);
        } catch (Exception ex) {
            log.warn("Nie udało się usunąć wpisu StorageKey (rollback)", ex);
        }
    }

    private void tryDeleteTempFile(Path tmpPath) {
        try {
            if (tmpPath != null) Files.deleteIfExists(tmpPath);
        } catch (IOException e) {
            log.warn("Nie udało się usunąć pliku tymczasowego", e);
        }
    }


}
