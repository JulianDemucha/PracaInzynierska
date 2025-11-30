package com.soundspace.service;

import com.soundspace.dto.ProcessedImage;
import com.soundspace.entity.StorageKey;
import com.soundspace.exception.ImageProcessingException;
import com.soundspace.exception.InvalidStorageLocationException;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final Tika tika;

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
    private final StorageService storageService;


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

    // do usuniecia wraz z endpointem /songs/covers/{storageKey}
    public Resource loadImageResource(String storageKey) {
        Resource resource = storageService.loadAsResource(storageKey);
        if(storageKey == null) throw new IllegalArgumentException("Klucz (storageKey) nie może być null");
        if ( !(storageKey.startsWith("songs/covers") || storageKey.startsWith("users/avatars")) )
            throw new InvalidStorageLocationException(storageKey);
        return resource;
    }

    public Resource loadImageResource(StorageKey storageKey) {
        Resource resource = storageService.loadAsResource(storageKey.getKey());

        String storageKeyStr = storageKey.getKey();
        if (!storageKeyStr.endsWith(".jpg") || !(storageKeyStr.startsWith("songs/covers") || storageKeyStr.startsWith("users/avatars") || storageKeyStr.startsWith("placeholders")))
            throw new InvalidStorageLocationException(storageKeyStr);

        return resource;
    }
}
