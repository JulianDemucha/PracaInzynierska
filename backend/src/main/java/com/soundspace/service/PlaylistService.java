package com.soundspace.service;

import com.soundspace.dto.PlaylistDto;
import com.soundspace.dto.PlaylistSongViewDto;
import com.soundspace.dto.ProcessedImage;
import com.soundspace.dto.request.CreatePlaylistRequest;
import com.soundspace.entity.*;
import com.soundspace.exception.AccessDeniedException;
import com.soundspace.exception.StorageException;
import com.soundspace.repository.PlaylistEntryRepository;
import com.soundspace.repository.PlaylistRepository;
import com.soundspace.repository.SongRepository;
import com.soundspace.repository.StorageKeyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

    /*
    todo:
        - dodanie playlisty
        - usuniecie playlisty (razem z jej piosenkami) (! uwzglednic position !)
        - dodawanie istniejacej piosenki do playlisty
        - usuwanie piosenki z playlisty
        - zmiana pozycji piosenki na playlistcie
     */

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

    private static final String COVERS_TARGET_DIRECTORY = "playlists/covers";
    private static final String TARGET_COVER_EXTENSION = "jpg";
    private static final int COVER_WIDTH = 1200;
    private static final int COVER_HEIGHT = 1200;
    private static final double COVER_QUALITY = 0.85;
    private final SongRepository songRepository;
    private final PlaylistEntryRepository playlistEntryRepository;

    // wip - tu bedzie sie mapowalo w playlistdto i zwracalo jakos
//    public List<PlaylistDto> getAllPlaylists() {
//        return playlistRepository.findAllByOrderByIdAsc();
//    }

    public List<PlaylistSongViewDto> getPlaylistSongs(Long playlistId) {
        return playlistEntryRepository.findAllSongsInPlaylist(playlistId)
                .stream().map(PlaylistSongViewDto::toDto).toList();
    }

    @Transactional
    public PlaylistDto createPlaylist(CreatePlaylistRequest request, UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        if (userEmail == null) throw new AccessDeniedException("User is not logged in");

        AppUser creator = appUserService.getUserByEmail(userEmail);

        Path tmpCoverPath = null;
        StorageKey coverStorageKeyEntity = null;

        try {
            MultipartFile coverFile = request.coverFile();
            if (coverFile == null || coverFile.isEmpty()) {
                throw new IllegalArgumentException("Plik okładki jest wymagany");
            }

            // resize, convert i zapis cover image do temp file
            tmpCoverPath = processCoverAndSaveToTemp(coverFile);

            // docelowy zapis cover image
            coverStorageKeyEntity = processAndSaveCoverFile(tmpCoverPath, creator);

            Playlist playlist = new Playlist();
            playlist.setName(request.title());
            playlist.setCreator(creator);
            playlist.setPubliclyVisible(request.publiclyVisible());
            playlist.setCreatedAt(Instant.now());
            playlist.setUpdatedAt(Instant.now());
            playlist.setCoverStorageKey(coverStorageKeyEntity);

            // zapis playlisty do db
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
    public PlaylistSongViewDto addSongToPlaylist(Long playlistId, Long songId, UserDetails userDetails) {

        String userEmail = userDetails.getUsername();
        if (userEmail == null) throw new AccessDeniedException("User is not logged in");

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono playlisty o id: " + playlistId));
        // todo zrobic custom exception jakis

        AppUser requestingUser = appUserService.getUserByEmail(userEmail);

        if (!playlist.getCreator().getId().equals(requestingUser.getId())) {
            throw new AccessDeniedException("Brak uprawnień do edycji playlisty");
        }

        if(playlistEntryRepository.existsBySongIdAndPlaylistId(songId, playlistId))
            throw new IllegalArgumentException("Piosenka już istnieje w albumie");

        // rzuci wyjatek jak piosenka nie istnieje, wiec nie potrzeba ponownej walidacji
        Song song = songCoreService.getSongById(songId);


        // jezeli song nie jest publiczny i:
        // - requestujacy nie jest jej autorem -> brak dostepu
        // - requestujacy jest jej autorem i album jest publiczny -> zmiana prywatnosci songa na publiczny
        if(!song.getPubliclyVisible()) {

            if(!requestingUser.getId().equals(song.getAuthor().getId())) {
                throw new AccessDeniedException("Brak uprawnień do piosenki");

            } else if (playlist.getPubliclyVisible()){
                song.setPubliclyVisible(true);
                songRepository.save(song);
                log.info("Zmieniono prywatnosc piosenki (id: {}) na publiczny po dodaniu do publicznej playlisty (id: {})",
                        song.getId(), playlist.getId());
            }

        }

        PlaylistEntry addedSong = playlist.addSong(song); // CascadeType.ALL, wiec przy zapisie playlisty PlaylistEntry tez sie zapisze

        playlist.setUpdatedAt(Instant.now());

        // zapis elegancji pzdr
        Playlist savedPlaylist = playlistRepository.save(playlist);

        log.info("Dodano piosenkę id={} do playlisty id={} (nowa pozycja: {})",
                song.getId(), playlist.getId(), savedPlaylist.getSongs().size() - 1);

        return PlaylistSongViewDto.toDto(addedSong);
    }


    /////////////////////////////////////** HELPERY *//////////////////////////////////////


    // resize i convert
    private Path processCoverAndSaveToTemp(MultipartFile coverFile) throws IOException {
        ProcessedImage processedCover = imageService.resizeImageAndConvert(
                coverFile,
                COVER_WIDTH,
                COVER_HEIGHT,
                TARGET_COVER_EXTENSION,
                COVER_QUALITY
        );

        Path tmpCoverPath = Files.createTempFile("playlist-cover-", "." + TARGET_COVER_EXTENSION);
        Files.write(tmpCoverPath, processedCover.bytes());
        return tmpCoverPath;
    }

    //  zapis pliku z temp file, utworzenie i zapisanie opdpowiadajacej plikowi encji StorageKey
    private StorageKey processAndSaveCoverFile(Path tmpCoverPath, AppUser appUser) throws IOException {
        long coverFileSize = Files.size(tmpCoverPath);
        String mimeType = "image/" + TARGET_COVER_EXTENSION;

        String coverStorageKeyString = storageService.saveFromPath(
                tmpCoverPath,
                appUser.getId(),
                TARGET_COVER_EXTENSION,
                COVERS_TARGET_DIRECTORY
        );
        log.info("Zapisano okładkę playlisty: key={}", coverStorageKeyString);

        StorageKey storageKeyEntity = new StorageKey();
        storageKeyEntity.setKey(coverStorageKeyString);
        storageKeyEntity.setMimeType(mimeType);
        storageKeyEntity.setSizeBytes(coverFileSize);

        return storageKeyRepository.save(storageKeyEntity);
    }

    private void rollbackStorage(StorageKey keyEntity) {
        if (keyEntity == null) return;
        try {
            storageService.delete(keyEntity.getKey());
        } catch (Exception ex) {
            log.warn("Nie udało się usunąć pliku ze storage (rollback): {}", keyEntity.getKey(), ex);
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
