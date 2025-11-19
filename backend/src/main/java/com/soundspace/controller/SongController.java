package com.soundspace.controller;

import com.soundspace.dto.SongDto;
import com.soundspace.security.dto.SongUploadRequest;
import com.soundspace.service.AppUserService;
import com.soundspace.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

// com.soundspace.controller.SongController
@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@Validated
public class SongController {

    private final SongService songService;
    private final AppUserService appUserService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<SongDto> upload(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute @Valid SongUploadRequest req){
        SongDto result = songService.upload(
                req.getFile(),
                req.getTitle(),
                req.getGenre(),
                appUserService.getUserByEmail(userDetails.getUsername()).getId(),
                req.getPubliclyVisible() != null && req.getPubliclyVisible()
        );
        URI location = URI.create("/api/songs/"+result.id());
        return ResponseEntity.created(location).body(result);
    }
}
