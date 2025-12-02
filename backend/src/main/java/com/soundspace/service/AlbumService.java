package com.soundspace.service;
import com.soundspace.dto.AlbumDto;
import com.soundspace.dto.ProcessedImage;
import com.soundspace.dto.projection.SongProjection;
import com.soundspace.dto.request.CreateAlbumRequest;
import com.soundspace.entity.Album;
import com.soundspace.entity.AppUser;
import com.soundspace.entity.Song;
import com.soundspace.entity.StorageKey;
import com.soundspace.enums.Genre;
import com.soundspace.exception.AccessDeniedException;
import com.soundspace.exception.AlbumNotFoundException;
import com.soundspace.exception.StorageException;
import com.soundspace.repository.AlbumRepository;
import com.soundspace.repository.PlaylistRepository;
import com.soundspace.repository.SongRepository;
import com.soundspace.repository.StorageKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    private static final String COVERS_TARGET_DIRECTORY = "albums/covers";
    private static final String TARGET_COVER_EXTENSION = "jpg";
    private static final int COVER_WIDTH = 1200;
    private static final int COVER_HEIGHT = 1200;
    private static final double COVER_QUALITY = 0.85;

    private static final Long DEFAULT_COVER_IMAGE_STORAGE_KEY_ID = 6767L;
    private final PlaylistRepository playlistRepository;

    public Optional<Album> findById(Long id) {
        if (id == null) return Optional.empty();
        return albumRepository.findById(id);
    }

    public AlbumDto getAlbumById(Long id, String userEmail) {
        Album album = findById(id).orElseThrow(
                () -> new AlbumNotFoundException(id));

        // jeżeli requestujący user nie jest autorem albumu i album jest prywatny - throw
        if (userEmail == null || (
                !album.getPubliclyVisible()
                        &&
                        !appUserService.getUserByEmail(userEmail).getId().equals(album.getAuthor().getId())
        )) throw new AccessDeniedException("Brak uprawnień");

        return AlbumDto.toDto(album);
    }

    public List<AlbumDto> findAllAlbumsByUserId(Long userId, String userEmail) {
        if (userEmail == null) throw new UsernameNotFoundException("Brak uprawnień");

        List<AlbumDto> albums = new java.util.ArrayList<>(albumRepository.findAllByAuthorId(userId)
                .stream()
                .map(AlbumDto::toDto)
                .toList());

        // jeżeli requestujący user nie jest tym samym co user w request'cie - remove niepubliczne utwory
        if (!userId.equals(appUserService.getUserByEmail(userEmail).getId()))
            albums.removeIf(album -> !album.publiclyVisible());

        return albums;
    }

    public List<AlbumDto> getPublicAlbumsByGenre(String genreName) {
        Genre genre = Genre.valueOf(genreName.toUpperCase().trim());

        return albumRepository.findAllByGenre(genre)
                .stream()
                .filter(Album::getPubliclyVisible)
                .map(AlbumDto::toDto)
                .toList();
    }

    @Transactional
    public AlbumDto createAlbum(CreateAlbumRequest request, String userEmail) {

        AppUser author = appUserService.getUserByEmail(userEmail);


        Path tmpCoverPath = null;
        StorageKey coverStorageKeyEntity = null;

        try{
            MultipartFile coverFile = request.getCoverFile();
            if(coverFile == null || coverFile.isEmpty()){
                throw new IllegalArgumentException("Plik okładki jest wymagany");
            }

            tmpCoverPath = processCoverAndSaveToTemp(coverFile);

            coverStorageKeyEntity = processAndSaveCoverFile(tmpCoverPath, author);

            List<Genre> validatedGenres = parseGenres(request.getGenre());

            Album album = Album.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .author(appUserService.getUserById(author.getId()))
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
        Album album = findById(albumId).orElseThrow(
                () -> new AlbumNotFoundException(albumId));

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
        for(SongProjection song : albumSongs){
            songCoreService.deleteSongById(song.getId(), userEmail);
        }
        StorageKey coverKey = album.getCoverStorageKey();
        albumRepository.delete(album);
        albumRepository.flush();

        try {
            if (coverKey != null && !coverKey.getId().equals(DEFAULT_COVER_IMAGE_STORAGE_KEY_ID) && coverKey.getKey() != null && !coverKey.getKey().isBlank()) {
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

    private Path processCoverAndSaveToTemp(MultipartFile coverFile) throws IOException {
        ProcessedImage processedCover =
                imageService.resizeImageAndConvert(coverFile, COVER_WIDTH, COVER_HEIGHT, TARGET_COVER_EXTENSION, COVER_QUALITY);

        Path tmpCoverPath = Files.createTempFile("album-cover-", "." + TARGET_COVER_EXTENSION);
        Files.write(tmpCoverPath, processedCover.bytes());
        return tmpCoverPath;
    }

    private StorageKey processAndSaveCoverFile(Path tmpCoverPath, AppUser appUser) throws IOException {
        long coverFileSize = Files.size(tmpCoverPath);
        // Pobieramy typ MIME (zakładamy image/jpeg po konwersji, ale można użyć Tiki dla pewności)
        String mimeType = "image/" + TARGET_COVER_EXTENSION;

        // Zapis fizyczny do storage
        String coverStorageKeyString = storageService.saveFromPath(
                tmpCoverPath,
                appUser.getId(),
                TARGET_COVER_EXTENSION,
                COVERS_TARGET_DIRECTORY
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
    public List<AlbumDto> getAllAlbums() {
        return albumRepository.findAllWithDetails()
                .stream()
                .map(AlbumDto::toDto)
                .toList();
    }

}
