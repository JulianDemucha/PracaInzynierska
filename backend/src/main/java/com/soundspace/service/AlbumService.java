package com.soundspace.service;

import com.soundspace.config.ApplicationConfigProperties;
import com.soundspace.dto.AlbumDto;
import com.soundspace.dto.ProcessedImage;
import com.soundspace.dto.SongDto;
import com.soundspace.dto.projection.SongProjection;
import com.soundspace.dto.request.AlbumCreateRequest;
import com.soundspace.dto.request.AlbumUpdateRequest;
import com.soundspace.entity.Album;
import com.soundspace.entity.AppUser;
import com.soundspace.entity.Song;
import com.soundspace.entity.StorageKey;
import com.soundspace.enums.Genre;
import com.soundspace.exception.AccessDeniedException;
import com.soundspace.exception.AlbumNotFoundException;
import com.soundspace.exception.StorageException;
import com.soundspace.repository.AlbumRepository;
import com.soundspace.repository.SongRepository;
import com.soundspace.repository.StorageKeyRepository;
import com.soundspace.service.song.SongCoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumService {
    private final AlbumRepository albumRepository;
    private final AppUserService appUserService;
    private final SongCoreService songCoreService;
    private final SongRepository songRepository;
    private final StorageService storageService;
    private final ImageService imageService;
    private final StorageKeyRepository storageKeyRepository;
    private final ApplicationConfigProperties.MediaConfig.CoverConfig coverConfig;
    
    public Optional<Album> findById(Long id) {
        if (id == null) return Optional.empty();
        return albumRepository.findById(id);
    }

    public Album getReferenceById(Long id) {
        return albumRepository.getReferenceById(id);
    }

    public AlbumDto getAlbumById(Long id, UserDetails userDetails) {
        Album album = findById(id).orElseThrow(
                () -> new AlbumNotFoundException(id));

        if (album.getPubliclyVisible()) return AlbumDto.toDto(album);


        // jeżeli requestujący user nie jest autorem albumu i album jest prywatny - throw
        if (userDetails == null || (
                !appUserService.getUserByEmail(userDetails.getUsername()).getId().equals(album.getAuthor().getId())
        )) throw new AccessDeniedException("Brak uprawnień");

        return AlbumDto.toDto(album);
    }

    public List<SongDto> getSongs(Long albumId, UserDetails userDetails) {
        Album album = albumRepository.getAlbumById(albumId);
        List<SongDto> songs = songRepository.findSongsByAlbumNative(albumId)
                .stream()
                .map(SongDto::toDto)
                .toList();

        // jesli publiczny to ok
        if (album.getPubliclyVisible())
            return songs;

        // jezeli nie-publiczny i niezalogowany -> throw
        if (userDetails == null)
            throw new AccessDeniedException("Ten album jest prywatny. Brak uprawnień");

        // jezeli nie-publiczny, zalogowany ale nie jest ownerem playlisty
        if (!appUserService.getUserByEmail(userDetails.getUsername()).getId().equals(album.getAuthor().getId()))
            throw new AccessDeniedException("Ten album jest prywatny. Brak uprawnień");

        // jesli jest ownerem
        return songs;
    }

    public List<AlbumDto> findAllAlbumsByUserId(Long userId, UserDetails userDetails) {
        if (userDetails == null)
            return albumRepository.findPublicByAuthorId(userId)
                    .stream()
                    .map(AlbumDto::toDto)
                    .toList();

            // if userId == requesting user id
        else if (appUserService.getUserByEmail(userDetails.getUsername()).getId().equals(userId))
            return albumRepository.findAllByAuthorId(userId)
                    .stream()
                    .map(AlbumDto::toDto)
                    .toList();

        return albumRepository.findPublicByAuthorId(userId)
                .stream()
                .map(AlbumDto::toDto)
                .toList();
    }

    public List<AlbumDto> getPublicAlbumsByGenre(String genreName, UserDetails userDetails) {
        try {
            Genre genre = Genre.valueOf(genreName.toUpperCase().trim());

            if (userDetails == null)
                return albumRepository.findPublicByGenre(genre)
                        .stream()
                        .map(AlbumDto::toDto)
                        .toList();

            else return albumRepository.findPublicOrOwnedByUserByGenre(genre, userDetails.getUsername())
                    .stream()
                    .map(AlbumDto::toDto)
                    .toList();

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Nieprawidłowy gatunek: " + genreName);
        }
    }

    @Transactional
    public AlbumDto createAlbum(AlbumCreateRequest request, String userEmail) {

        AppUser author = appUserService.getUserByEmail(userEmail);


        Path tmpCoverPath = null;
        StorageKey coverStorageKeyEntity = null;

        try {
            MultipartFile coverFile = request.getCoverFile();
            if (coverFile == null || coverFile.isEmpty()) {
                coverStorageKeyEntity = storageKeyRepository.getReferenceById(coverConfig.defaultCoverId());

            } else {
                tmpCoverPath = processCoverAndSaveToTemp(coverFile);

                coverStorageKeyEntity = processAndSaveCoverFile(tmpCoverPath, author);
            }


            List<Genre> validatedGenres = parseGenres(request.getGenre());

            Album album = Album.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .author(author)
                    .publiclyVisible(request.isPubliclyVisible())
                    .createdAt(Instant.now())
                    .coverStorageKey(coverStorageKeyEntity)
                    .genres(validatedGenres)
                    .build();
            album = albumRepository.save(album);
            return AlbumDto.toDto(album);

        } catch (Exception e) {
            log.error("Błąd podczas tworzenia albumu", e);

            rollbackStorage(coverStorageKeyEntity);

            if (e instanceof IOException) {
                throw new StorageException("Błąd I/O podczas przetwarzania okładki albumu", e);
            }
            throw (RuntimeException) e;

        } finally {
            tryDeleteTempFile(tmpCoverPath);
        }


    }

    @Transactional
    public void removeAlbumSong(Long albumId, Long songId, String userEmail) {
        if (albumRepository.existsById(albumId))
            throw new AlbumNotFoundException(albumId);


        Song song = songCoreService.getSongById(songId);

        if (song.getCoverStorageKey() != null) {
            song.setCoverStorageKey(null);
            songRepository.save(song);
        }
        songCoreService.deleteSongById(songId, userEmail);
    }

    @Transactional
    public void deleteAlbum(Long albumId, String userEmail) {
        Album album = findById(albumId).orElseThrow(
                () -> new AlbumNotFoundException(albumId));

        // jeżeli requestujący user nie jest autorem albumu - throw
        if (userEmail == null || !appUserService.getUserByEmail(userEmail).getId()
                .equals(album.getAuthor().getId()))
            throw new AccessDeniedException("Brak uprawnień");

        List<SongProjection> albumSongs = songRepository.findSongsByAlbumNative(albumId);
        for (SongProjection song : albumSongs) {
            songCoreService.deleteSongById(song.getId(), userEmail);
        }
        StorageKey coverKey = album.getCoverStorageKey();
        albumRepository.delete(album);
        albumRepository.flush();

        try {
            if (coverKey != null && !coverKey.getId().equals(coverConfig.defaultCoverId()) && coverKey.getKey() != null && !coverKey.getKey().isBlank()) {
                try {
                    storageService.delete(coverKey.getKey());
                } catch (Exception ex) {
                    log.warn("Nie udało się usunąć pliku cover z storage: {}", coverKey.getKey(), ex);
                    throw ex;
                }
                try {
                    storageKeyRepository.deleteById(coverKey.getId());
                } catch (Exception ex) {
                    log.warn("Nie udało się usunąć rekordu storage_keys (album cover) id={}: {}", coverKey.getId(), ex.getMessage());
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
    public AlbumDto update(Long albumId, AlbumUpdateRequest request, UserDetails userDetails) { // @AuthenticationPrincipal userDetails jest NotNull
        Album updatedAlbum = albumRepository.findById(albumId).orElseThrow(); //narazie wszytkie pola takie same, a pozniej beda zmieniane zeby zapisac po update

        AppUser user = appUserService.getUserByEmail(userDetails.getUsername());
        if (!updatedAlbum.getAuthor().getId().equals(user.getId()))
            throw new AccessDeniedException("Brak dostępu do edycji piosenki");

        MultipartFile coverFile = request.coverFile();
        StorageKey storageKeyToDelete = null;
        if (coverFile != null && !coverFile.isEmpty()) {
            storageKeyToDelete = updatedAlbum.getCoverStorageKey();
            updatedAlbum.setCoverStorageKey(imageService.processAndSaveNewImage(
                    coverFile,
                    user,
                    coverConfig.width(),
                    coverConfig.height(),
                    coverConfig.quality(),
                    coverConfig.targetExtension(),
                    coverConfig.albumDirectory(),
                    "cover"
            ));
        }

        if (request.title() != null) {
            updatedAlbum.setTitle(request.title());
        }

        if (request.description() != null) {
            updatedAlbum.setDescription(request.description());
        }

        if (request.publiclyVisible() != null) {
            updatedAlbum.setPubliclyVisible(request.publiclyVisible());
            albumRepository.refreshPubliclyVisibleInAlbumSongs(albumId);
        }

        albumRepository.save(updatedAlbum);
        albumRepository.flush();
        albumRepository.refreshCoverStorageKeyInAlbumSongs(albumId);
        if (storageKeyToDelete != null) {
            imageService.cleanUpOldImage(storageKeyToDelete, "cover");
        }
        return AlbumDto.toDto(updatedAlbum);
    }

    private Path processCoverAndSaveToTemp(MultipartFile coverFile) throws IOException {
        ProcessedImage processedCover =
                imageService.resizeImageAndConvert(coverFile, coverConfig.width(), coverConfig.height(), coverConfig.targetExtension(), coverConfig.quality());

        Path tmpCoverPath = Files.createTempFile("album-cover-", "." + coverConfig.targetExtension());
        Files.write(tmpCoverPath, processedCover.bytes());
        return tmpCoverPath;
    }

    private StorageKey processAndSaveCoverFile(Path tmpCoverPath, AppUser appUser) throws IOException {
        long coverFileSize = Files.size(tmpCoverPath);
        // Pobieramy typ MIME (zakładamy image/jpeg po konwersji, ale można użyć Tiki dla pewności)
        String mimeType = "image/" + coverConfig.targetExtension();

        // Zapis fizyczny do storage
        String coverStorageKeyString = storageService.saveFromPath(
                tmpCoverPath,
                appUser.getId(),
                coverConfig.targetExtension(),
                coverConfig.albumDirectory()
        );
        log.info("Zapisano okładkę albumu: key={}", coverStorageKeyString);

        // Zapis metadanych StorageKey
        StorageKey storageKeyEntity = new StorageKey();
        storageKeyEntity.setKey(coverStorageKeyString);
        storageKeyEntity.setMimeType(mimeType);
        storageKeyEntity.setSizeBytes(coverFileSize);

        return storageKeyRepository.save(storageKeyEntity);
    }

    private List<Genre> parseGenres(List<String> genreStrings) {
        List<Genre> validatedGenreList = new ArrayList<>();
        if (genreStrings == null) return validatedGenreList;

        try {
            for (String genre : genreStrings)
                if (genre != null && !genre.isBlank())
                    validatedGenreList.add(Genre.valueOf(genre.toUpperCase().trim()));

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Nieprawidłowy gatunek (genre) w liście");
        }
        return validatedGenreList;
    }

    private void rollbackStorage(StorageKey keyEntity) {
        if (keyEntity == null) return;
        cleanUpStorageKey(keyEntity);
    }

    private void cleanUpStorageKey(StorageKey keyEntity) {
        try {
            storageService.delete(keyEntity.getKey());
        } catch (Exception ex) {
            log.warn("Nie udało się usunąć pliku ze storage: {}", keyEntity.getKey(), ex);
        }
        try {
            storageKeyRepository.delete(keyEntity);
        } catch (Exception ex) {
            log.warn("Nie udało się usunąć wpisu StorageKey", ex);
        }
    }

    private void tryDeleteTempFile(Path tmpPath) {
        try {
            if (tmpPath != null) Files.deleteIfExists(tmpPath);
        } catch (IOException e) {
            log.warn("Nie udało się usunąć pliku tymczasowego", e);
        }
    }

    // todo switch to PAGINATION
    @Transactional(readOnly = true)
    public List<AlbumDto> getAllAlbums(UserDetails userDetails) {
        if (userDetails == null) return albumRepository.findAllPublic()
                .stream()
                .map(AlbumDto::toDto)
                .toList();

        else return albumRepository.findAllPublicOrOwnedByUser(userDetails.getUsername())
                .stream()
                .map(AlbumDto::toDto)
                .toList();
    }

    private List<SongDto> getSongsFromSongProjection(List<SongProjection> songsProjection) {
        return songsProjection.stream()
                .map(SongDto::toDto).toList();
    }

}
