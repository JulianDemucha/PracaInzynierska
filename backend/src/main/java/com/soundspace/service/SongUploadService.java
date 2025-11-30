package com.soundspace.service;

import com.soundspace.dto.ProcessedImage;
import com.soundspace.dto.SongDto;
import com.soundspace.dto.request.AlbumSongUploadRequest;
import com.soundspace.entity.Album;
import com.soundspace.entity.AppUser;
import com.soundspace.entity.Song;
import com.soundspace.entity.StorageKey;
import com.soundspace.enums.Genre;
import com.soundspace.exception.SongUploadException;
import com.soundspace.repository.SongRepository;
import com.soundspace.repository.StorageKeyRepository;
import com.soundspace.dto.request.SongUploadRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class SongUploadService {

    private final StorageService storage;
    private final SongRepository songRepository;
    private final StorageKeyRepository storageKeyRepository;
    private final Tika tika;
    private final ImageService imageService;
    private final AlbumService albumService;

    private static final int MAX_BYTES = 100 * 1024 * 1024; // 100MB
    private static final String SONGS_TARGET_DIRECTORY = "songs/audio";
    private static final String COVERS_TARGET_DIRECTORY = "songs/covers";
    private static final String TARGET_AUDIO_EXTENSION = "m4a"; // service wpuszcza tylko .m4a
    private static final String TARGET_COVER_EXTENSION = "jpg"; // jest konwersja

    /// typowy upload piosenki poza albumem (singiel)
    @Transactional
    public SongDto upload(SongUploadRequest request, AppUser appUser) {
        MultipartFile audioFile = request.getAudioFile();
        MultipartFile coverFile = request.getCoverFile();

        Path tmpAudioPath = null;
        Path tmpCoverPath = null;
        StorageKey audioStorageKeyEntity = null;
        StorageKey coverStorageKeyEntity = null;

        try {
            // walidacja audio i zapis do temp file
            tmpAudioPath = validateSongFileAndSaveToTemp(audioFile);

            // docelowy zapis audio
            audioStorageKeyEntity = validateAndSaveAudioFile(tmpAudioPath, appUser);

            // resize i convert okladki i zapis do temp file
            tmpCoverPath = processCoverAndSaveToTemp(coverFile);

            // docelowy zapis okladki
            coverStorageKeyEntity = processAndSaveCoverFile(tmpCoverPath, appUser);

            Song s = validateAndBuildSong(
                    request,
                    appUser,
                    audioStorageKeyEntity,
                    coverStorageKeyEntity
            );

            Song saved = songRepository.save(s);
            return SongDto.toDto(saved);

        } catch (Exception e) {
            log.error("Błąd I/O podczas uploadu pliku", e);

            rollbackStorage(audioStorageKeyEntity);
            rollbackStorage(coverStorageKeyEntity);

            if (e instanceof IOException) {
                throw new SongUploadException("Błąd I/O podczas uploadu pliku", e);
            }
            throw (RuntimeException) e;

        } finally {
            tryDeleteTempFile(tmpAudioPath);
            tryDeleteTempFile(tmpCoverPath);
        }
    }

    /// upload piosenki z perspektywy albumu
    @Transactional
    public SongDto upload(Long albumId, AlbumSongUploadRequest request, AppUser appUser) {
        MultipartFile audioFile = request.getAudioFile();

        Path tmpAudioPath = null;
        StorageKey audioStorageKeyEntity = null;

        try {
            // walidacja audio i zapis do temp file
            tmpAudioPath = validateSongFileAndSaveToTemp(audioFile);

            // docelowy zapis audio
            audioStorageKeyEntity = validateAndSaveAudioFile(tmpAudioPath, appUser);

            Song s = validateAndBuildSong(
                    albumId,
                    request,
                    appUser,
                    audioStorageKeyEntity
            );

            Song saved = songRepository.save(s);
            return SongDto.toDto(saved);

        } catch (Exception e) {
            log.error("Błąd I/O podczas uploadu pliku", e);

            rollbackStorage(audioStorageKeyEntity);
            if (e instanceof IOException) {
                throw new SongUploadException("Błąd I/O podczas uploadu pliku", e);
            }

            throw (RuntimeException) e;

        } finally {
            tryDeleteTempFile(tmpAudioPath);
        }
    }

    // wersja dla singla
    private Song validateAndBuildSong(SongUploadRequest request, AppUser appUser,
                                      StorageKey audioStorageKeyEntity, StorageKey coverStorageKeyEntity)
            throws IllegalArgumentException {

        String title = request.getTitle();

        if (title == null || title.isBlank() || title.length() > 32) {
            throw new IllegalArgumentException("Nieprawidłowy tytuł");
        }

        List<Genre> validatedGenreList = new ArrayList<>();
        try {
            for (String genre : request.getGenre())
                if (genre != null && !genre.isBlank())
                    validatedGenreList.add(Genre.valueOf(genre.toUpperCase().trim()));

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Nieprawidłowy gatunek (genre)");
        }

        Song s = new Song();
        s.setTitle(title);
        s.setAuthor(appUser);
        s.setGenres(validatedGenreList);
        s.setAlbum(null);
        s.setAudioStorageKey(audioStorageKeyEntity);
        s.setCoverStorageKey(coverStorageKeyEntity);
        s.setPubliclyVisible(request.getPubliclyVisible());
        s.setCreatedAt(Instant.now());
        return s;
    }

    // wersja dla albumu
    private Song validateAndBuildSong(Long albumId, AlbumSongUploadRequest request, AppUser appUser,
                                      StorageKey audioStorageKeyEntity)
            throws IllegalArgumentException {
        Album album = albumService.findById(albumId).orElseThrow();

        String title = request.getTitle();

        if (title == null || title.isBlank() || title.length() > 32) {
            throw new IllegalArgumentException("Nieprawidłowy tytuł");
        }

        Song s = new Song();
        s.setTitle(title);
        s.setAuthor(appUser);
        s.setGenres(new ArrayList<>(album.getGenres()));
        s.setAlbum(album);
        s.setAudioStorageKey(audioStorageKeyEntity);
        s.setCoverStorageKey(album.getCoverStorageKey());
        s.setPubliclyVisible(album.getPubliclyVisible());
        s.setCreatedAt(Instant.now());
        return s;
    }

    private Path processCoverAndSaveToTemp(MultipartFile coverFile) throws IOException {
        ProcessedImage processedCover =
                imageService.resizeImageAndConvert(coverFile, 1200, 1200, TARGET_COVER_EXTENSION, 0.80);

        Path tmpCoverPath = Files.createTempFile("cover-", "." + TARGET_COVER_EXTENSION);
        Files.write(tmpCoverPath, processedCover.bytes());
        return tmpCoverPath;
    }

    private Path validateSongFileAndSaveToTemp(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Brak pliku w żądaniu");
        }

        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("Plik jest za duży");
        }

        Path tmp = Files.createTempFile("upload-", "." + TARGET_AUDIO_EXTENSION /* i tak service wpuszcza tylko .m4a */);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
        }

        return tmp;
    }

    private String detectAndValidateAudioFileMimeType(Path tmp) throws IOException {
        String detected = tika.detect(tmp.toFile());
        boolean okDetected = detected != null && detected.startsWith("audio") &&
                (detected.contains("mp4") || detected.contains("m4a") ||
                        detected.equalsIgnoreCase("audio/mp4")); // tylko .m4a

        if (!okDetected) {
            throw new IllegalArgumentException("Niespójna zawartość pliku");
        }
        return detected;
    }

    private StorageKey validateAndSaveAudioFile(Path tmpAudioPath, AppUser appUser) throws IOException {
        // walidacja audio i zapis do temp file
        String audioFileMimeType = detectAndValidateAudioFileMimeType(tmpAudioPath);
        long audioFileSize = Files.size(tmpAudioPath);

        // docelowy zapis audio
        String audioStorageKey = storage.saveFromPath(tmpAudioPath, appUser.getId(), TARGET_AUDIO_EXTENSION, SONGS_TARGET_DIRECTORY);
        log.info("Zapisano plik: audioStorageKey={}", audioStorageKey);

        // StorageKey dla audio
        StorageKey audioStorageKeyEntity = new StorageKey();
        audioStorageKeyEntity.setKey(audioStorageKey);
        audioStorageKeyEntity.setMimeType(audioFileMimeType);
        audioStorageKeyEntity.setSizeBytes(audioFileSize);
        audioStorageKeyEntity = storageKeyRepository.save(audioStorageKeyEntity);
        return audioStorageKeyEntity;
    }

    private StorageKey processAndSaveCoverFile(Path tmpCoverPath, AppUser appUser) throws IOException {
        // resize i convert cover image i zapis do temp file
        String coverFileMimeType = tika.detect(tmpCoverPath.toFile());
        long coverFileSize = Files.size(tmpCoverPath);

        // docelowy zapis cove
        String coverStorageKey = storage.saveFromPath(tmpCoverPath, appUser.getId(), TARGET_COVER_EXTENSION, COVERS_TARGET_DIRECTORY);
        log.info("Zapisano plik: coverStorageKey={}", coverStorageKey);

        // StorageKey dla cover
        StorageKey coverStorageKeyEntity = new StorageKey();
        coverStorageKeyEntity.setKey(coverStorageKey);
        coverStorageKeyEntity.setMimeType(coverFileMimeType);
        coverStorageKeyEntity.setSizeBytes(coverFileSize);
        coverStorageKeyEntity = storageKeyRepository.save(coverStorageKeyEntity);
        return coverStorageKeyEntity;
    }

    private void rollbackStorage(StorageKey... keys) {
        for (StorageKey keyEntity : keys) {
            if (keyEntity == null) continue;
            try {
                storage.delete(keyEntity.getKey());
            } catch (Exception ex) {
                log.warn("Nie udało się usunąć pliku ze storage podczas rollbacku: {}", keyEntity.getKey(), ex);
            }
            try {
                storageKeyRepository.delete(keyEntity);
            } catch (Exception ex) {
                log.warn("Nie udało się usunąć wpisu StorageKey podczas rollbacku", ex);
            }
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
