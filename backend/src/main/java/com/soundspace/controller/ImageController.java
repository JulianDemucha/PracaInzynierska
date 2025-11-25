package com.soundspace.controller;

import com.soundspace.entity.StorageKey;
import com.soundspace.service.ImageService;
import com.soundspace.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;
    private final StorageService storageService;

    @GetMapping("/{storageKeyId}")
    public ResponseEntity<Resource> getFile(@PathVariable Long storageKeyId) {
        StorageKey coverStorageKey = storageService.getStorageKey(storageKeyId);
        Resource resource = imageService.loadImageResource(coverStorageKey);
        String contentType = coverStorageKey.getMimeType();

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(contentType);

        } catch (InvalidMediaTypeException e) {
            mediaType = MediaType.IMAGE_JPEG; //fallback
        }

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "max-age=43200, public"); // cache 12h
        //// !!! po zmianie obrazka przez 12h w cache dalej bedzie stary !!!

        return builder.body(resource);
    }
}