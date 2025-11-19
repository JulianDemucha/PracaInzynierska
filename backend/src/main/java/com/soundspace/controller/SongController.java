package com.soundspace.controller;

import com.soundspace.dto.SongDto;
import com.soundspace.security.dto.SongUploadRequest;
import com.soundspace.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// com.soundspace.controller.SongController
@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@Validated
public class SongController {

    private final SongService songService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<SongDto> upload(@ModelAttribute @Valid SongUploadRequest req) {
        SongDto result = songService.upload(
                req.getFile(),
                req.getTitle(),
                req.getGenre(),
                req.getUserId(),
                req.getPubliclyVisible() != null && req.getPubliclyVisible()
        );
        return ResponseEntity.ok(result);
    }
}
