package com.soundspace.service;

import com.soundspace.dto.ProcessedImage;
import com.soundspace.dto.SongDto;
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

    @Transactional
    public SongDto upload(SongUploadRequest request, AppUser appUser) {
        MultipartFile audioFile = request.getAudioFile();
        MultipartFile coverFile = request.getCoverFile();

        Path tmpAudioPath = null;
        Path tmpCoverPath = null;

        String audioStorageKey;
        String coverStorageKey;

        StorageKey audioStorageKeyEntity;
        StorageKey coverStorageKeyEntity;

        try {
            // walidacja audio i zapis do temp file
            tmpAudioPath = validateSongFileAndSaveToTemp(audioFile);
            String audioFileMimeType = detectAndValidateAudioFileMimeType(tmpAudioPath);
            long audioFileSize = Files.size(tmpAudioPath);

            // docelowy zapis audio
            audioStorageKey = storage.saveFromPath(tmpAudioPath, appUser.getId(), TARGET_AUDIO_EXTENSION, SONGS_TARGET_DIRECTORY);
            log.info("Zapisano plik: audioStorageKey={}", audioStorageKey);

            // StorageKey dla audio
            audioStorageKeyEntity = new StorageKey();
            audioStorageKeyEntity.setKey(audioStorageKey);
            audioStorageKeyEntity.setMimeType(audioFileMimeType);
            audioStorageKeyEntity.setSizeBytes(audioFileSize);
            audioStorageKeyEntity = storageKeyRepository.save(audioStorageKeyEntity);

            // resize i convert cover image i zapis do temp file
            tmpCoverPath = processCoverAndSaveToTemp(coverFile);
            String coverFileMimeType = tika.detect(tmpCoverPath.toFile());
            long coverFileSize = Files.size(tmpCoverPath);

            // docelowy zapis cove
            coverStorageKey = storage.saveFromPath(tmpCoverPath, appUser.getId(), TARGET_COVER_EXTENSION, COVERS_TARGET_DIRECTORY);
            log.info("Zapisano plik: coverStorageKey={}", coverStorageKey);

            // StorageKey dla cover
            coverStorageKeyEntity = new StorageKey();
            coverStorageKeyEntity.setKey(coverStorageKey);
            coverStorageKeyEntity.setMimeType(coverFileMimeType);
            coverStorageKeyEntity.setSizeBytes(coverFileSize);
            coverStorageKeyEntity = storageKeyRepository.save(coverStorageKeyEntity);

            Song s = validateAndBuildSong(
                    request,
                    appUser,
                    audioStorageKeyEntity,
                    coverStorageKeyEntity
            );

            try {
                Song saved = songRepository.save(s);
                return SongDto.toDto(saved);
            } catch (Exception e) {
                /// cleanup: usuwanie plików ze storage i rekordy storage_keys - jesli zapisywanie sie nie powiodlo

                log.error("Błąd w trakcie zapisu piosenki do bazy danych", e);
                try {
                    if (audioStorageKey != null) storage.delete(audioStorageKey);

                } catch (Exception ex) {
                    log.warn("Błąd usuwania pliku audio po nieudanym zapisie piosenki", ex);
                }

                try {
                    if (coverStorageKey != null) storage.delete(coverStorageKey);

                } catch (Exception ex) {
                    log.warn("Błąd usuwania pliku cover po nieudanym zapisie piosenki", ex);
                }

                try {
                    storageKeyRepository.delete(audioStorageKeyEntity);

                } catch (Exception ex) {
                    log.warn("Błąd usuwania rekordu storageKey audio po nieudanym zapisie piosenki", ex);
                }

                try {
                    storageKeyRepository.delete(coverStorageKeyEntity);

                } catch (Exception ex) {
                    log.warn("Błąd usuwania rekordu storageKey cover po nieudanym zapisie piosenki", ex);
                }

                throw e;
            }

        } catch (IOException e) {
            log.error("Błąd I/O podczas uploadu pliku", e);
            throw new SongUploadException("Błąd I/O podczas uploadu pliku", e);

        } finally {
            try {
                if (tmpAudioPath != null) Files.deleteIfExists(tmpAudioPath);

            } catch (IOException e) {
                log.warn("Nie udało się usunąć pliku tymczasowego audio", e);
            }

            try {
                if (tmpCoverPath != null) Files.deleteIfExists(tmpCoverPath);

            } catch (IOException e) {
                log.warn("Nie udało się usunąć pliku tymczasowego cover", e);
            }
        }
    }

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
        s.setAlbum(albumService.findById(request.getAlbumId()).orElse(null));
        s.setAudioStorageKey(audioStorageKeyEntity);
        s.setCoverStorageKey(coverStorageKeyEntity);
        s.setPubliclyVisible(request.getPubliclyVisible());
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
}
