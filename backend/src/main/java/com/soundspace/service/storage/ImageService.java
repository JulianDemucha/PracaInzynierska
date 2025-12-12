package com.soundspace.service.storage;

import com.soundspace.dto.ProcessedImage;
import com.soundspace.entity.AppUser;
import com.soundspace.entity.StorageKey;
import com.soundspace.exception.ImageProcessingException;
import com.soundspace.exception.InvalidStorageLocationException;
import com.soundspace.exception.StorageException;
import com.soundspace.repository.StorageKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final Tika tika;
    private final StorageService storageService;
    private final StorageKeyRepository storageKeyRepository;

    private static final int MAX_BYTES = 100 * 1024 * 1024;

    private static final Set<String> ALLOWED_MIMES = Set.of(
            "image/jpg", "image/jpeg", "image/png", "image/webp", "image/avif"
    );

    private static final Map<String, String> FORMAT_TO_MIME = Map.of(
            "jpg", MediaType.IMAGE_JPEG_VALUE,
            "jpeg", MediaType.IMAGE_JPEG_VALUE,
            "png", MediaType.IMAGE_PNG_VALUE,
            "webp", "image/webp",
            "avif", "image/avif"
    );



    public ProcessedImage resizeImageAndConvert(MultipartFile imageFile, int width, int height, String outputFormat, double quality) {
        if (outputFormat == null || outputFormat.isBlank()) {
            throw new IllegalArgumentException("outputFormat nie może być null/empty");
        }
        validateImage(imageFile);

        String validatedOutFormat = outputFormat.toLowerCase();
        String contentType = mimeFromFormat(validatedOutFormat);

        if (contentType == null) {
            throw new IllegalArgumentException("Niedozwolony format wyjściowy: " + outputFormat);
        }

        try {
            byte[] outBytes = resizeToBytes(imageFile, width, height, validatedOutFormat, quality);

            String originalName = imageFile.getOriginalFilename();
            String baseName = (originalName == null) ? "image" : originalName.replaceAll("\\.[^.]+$", "");
            String newFilename = baseName + "." + validatedOutFormat;

            return new ProcessedImage(outBytes, newFilename, contentType);
        } catch (IOException e) {
            throw new ImageProcessingException("Błąd podczas tworzenia przetworzonego obrazu", e);
        }
    }

    private byte[] resizeToBytes(MultipartFile imageFile,
                                int width,
                                int height,
                                String outputFormat,
                                double quality) throws IOException {
        byte[] inputBytes = imageFile.getBytes();

        try (ByteArrayInputStream in = new ByteArrayInputStream(inputBytes);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Thumbnails.of(in)
                    .size(width, height)
                    .outputFormat(outputFormat)
                    .outputQuality(quality)
                    .toOutputStream(out);

            return out.toByteArray();
        }
    }

    private void validateImage(MultipartFile imageFile) {
        try {
            byte[] inputBytes = imageFile.getBytes();

            if (inputBytes.length == 0) {
                throw new IllegalArgumentException("Pusty plik");
            }
            if (inputBytes.length > MAX_BYTES) {
                throw new IllegalArgumentException("Plik za duży (limit " + (MAX_BYTES / (1024*1024)) + " MB)");
            }

            String detected = tika.detect(new ByteArrayInputStream(inputBytes));
            if (detected == null || !detected.startsWith("image/")) {
                throw new IllegalArgumentException("Plik nie jest obrazem");
            }
            if (!ALLOWED_MIMES.contains(detected)) {
                throw new IllegalArgumentException("Niedozwolony format wejściowy: " + detected);
            }
        } catch (IOException e) {
            throw new ImageProcessingException("Błąd podczas przetwarzania pliku w trakcie walidacji", e);
        }
    }

    private String mimeFromFormat(String format) {
        if (format == null) return null;
        return FORMAT_TO_MIME.get(format.toLowerCase());
    }

    public Resource loadImageResource(StorageKey storageKey) {
        Resource resource = storageService.loadAsResource(storageKey.getKey());

        String storageKeyStr = storageKey.getKey();
        if (
                !storageKeyStr.endsWith(".jpg") ||

                !(
                storageKeyStr.startsWith("songs/covers") ||
                storageKeyStr.startsWith("users/avatars") ||
                storageKeyStr.startsWith("albums/covers") ||
                storageKeyStr.startsWith("placeholders") ||
                storageKeyStr.startsWith("playlists/covers")
                )

        )
            throw new InvalidStorageLocationException
                    ("Nie można pobrać pliku: niedozwolona ścieżka lub nieobsługiwany typ pliku (wymagany .jpg).");

        return resource;
    }

    public StorageKey processAndSaveNewImage(MultipartFile file, AppUser user,
                                             int width, int height, double quality,
                                             String targetExtension, String targetDirectory, String prefix) { // prefix np "avatar"
        Path tmpAvatar = null;

        try {
            // resize/convert -> ProcessedImage
            var processed = resizeImageAndConvert(file, width, height, targetExtension, quality);

            // zapis do temp file
            tmpAvatar = Files.createTempFile(prefix+"-", "." + targetExtension);
            Files.write(tmpAvatar, processed.bytes());

            // zapis do storage
            String storageKey = storageService.saveFromPath(tmpAvatar, user.getId(), targetExtension, targetDirectory);
            log.info("Zapisano do storage: {}", storageKey);

            // zapis encji StorageKey
            StorageKey sk = new StorageKey();
            sk.setKey(storageKey);
            sk.setMimeType(processed.contentType());
            sk.setSizeBytes(processed.bytes().length);
            sk = storageKeyRepository.save(sk);

            return sk;

            //todo rzucic tu jakies custom exceptiony i handlowac na http status

        } catch (IOException e) {
            throw new StorageException("Błąd zapisu pliku"+" {"+prefix+"}", e);

        } finally {
            if (tmpAvatar != null) {
                try {
                    Files.deleteIfExists(tmpAvatar);
                } catch (IOException ex) {
                    log.warn("Nie udalo sie usunac temp file"+" {"+prefix+"}", ex);
                }
            }
        }
    }

    public void cleanUpOldImage(StorageKey imageStorageKey, String prefix) {
        // usuwanie poprzedniego image jezeli to nie default
        if (imageStorageKey != null && !imageStorageKey.getId().equals(6767L)) { // 6767L = DEFAULT_AVATAR_IMAGE_STORAGE_KEY_ID
            try {
                storageService.delete(imageStorageKey.getKey());
            } catch (Exception ex) {
                log.warn("Nie udało się usunąć pliku starego image "+" {"+prefix+")"+" z storage: {}", imageStorageKey.getKey(), ex);
            }
            try {
                storageKeyRepository.delete(imageStorageKey);
            } catch (Exception ex) {
                log.warn("Nie udało się usunąć wpisu StorageKey starego image "+" ("+prefix+")"+": id={}", imageStorageKey.getId(), ex);
            }
        }
    }
}
