package com.soundspace.service;

import com.soundspace.dto.SongDto;
import com.soundspace.entity.AppUser;
import com.soundspace.entity.Song;
import com.soundspace.enums.Genre;
import com.soundspace.repository.AppUserRepository;
import com.soundspace.repository.SongRepository;
import com.soundspace.security.dto.SongUploadRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
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
    private final AppUserRepository appUserRepository;

    private static final int MAX_BYTES = 100 * 1024 * 1024; // 100MB
    private static final String saveSubDirectory = "songs"; // ze np /data/songs jak tu
    private final String TARGET_EXTENSION = "m4a"; // service wpuszcza tyllko .m4a
    private final Tika tika = new Tika();


    @Transactional
    public SongDto upload(SongUploadRequest request, AppUser appUser) {
        MultipartFile file = request.getFile();


        Path tmpPath = null;

        try {
            tmpPath = validateSongFileAndSaveToTemp(file);
            String mimeType = detectAndValidateFileMimeType(tmpPath);
            long fileSize = Files.size(tmpPath);

            // docelowy zapis
            String storageKey = storage.saveFromPath(tmpPath, appUser.getId(), TARGET_EXTENSION, saveSubDirectory);
            log.info("Zapisano plik: storageKey={}", storageKey);

            Song s = validateAndBuildSong(
                    request,
                    appUser,
                    storageKey,
                    fileSize,
                    mimeType
            );

            try {

                Song savedSong = songRepository.save(s);

                List<String> dtoGenres = new ArrayList<>();
                for (Genre genre : savedSong.getGenres()) {
                    dtoGenres.add(genre.toString());
                }

                return SongDto.builder()
                        .id(savedSong.getId())
                        .title(savedSong.getTitle())
                        .authorUsername(savedSong.getAuthor().getUsername())
                        .publiclyVisible(savedSong.getPubliclyVisible())
                        .genres(dtoGenres)
                        .createdAt(savedSong.getCreatedAt().toString())
                        .build();

            } catch (Exception e) {

                try {
                    storage.delete(storageKey);
                } catch (Exception e1) {
                    log.warn("Błąd w trakcie usuwania pliku", e1);
                }
                log.error("Błąd w trakcie zapisu piosenki do bazy danych", e);
                throw e;
            }


        } catch (IOException e) {
            log.error("Błąd I/O podczas uploadu pliku", e);
            throw new UncheckedIOException("Błąd I/O podczas uploadu pliku", e);

        } finally {
            try {
                if (tmpPath != null) Files.deleteIfExists(tmpPath);
            } catch (IOException e) {
                log.warn("Nie udało się usunąć pliku tymczasowego", e);
            }
        }
    }

    private Song validateAndBuildSong(SongUploadRequest request, AppUser appUser,
                                      String storageKey, long sizeBytes, String mimeType)
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
        s.setPubliclyVisible(request.getPubliclyVisible());
        s.setStorageKey(storageKey);
        s.setMimeType(mimeType);
        s.setSizeBytes(sizeBytes);
        s.setCreatedAt(Instant.now());
        return s;
    }

    private Path validateSongFileAndSaveToTemp(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Brak pliku w żądaniu");
        }

        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("Plik jest za duży");
        }

        Path tmp = Files.createTempFile("upload-", "." + TARGET_EXTENSION /* i tak service wpuszcza tylko .m4a */);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
        }

        return tmp;
    }

    private String detectAndValidateFileMimeType(Path tmp) throws IOException {
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
