package com.soundspace.controller.song;
import com.soundspace.service.song.SongStreamingService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@Validated
public class SongStreamController {
    private final SongStreamingService songStreamingService;

    @GetMapping("/stream/{songId}")
    public ResponseEntity<ResourceRegion> streamSong(
            @PathVariable Long songId,
            @RequestHeader(value = "Range", required = false) String rangeHeader,
            Authentication authentication) {

        try {

            ResourceRegion region = songStreamingService.getSongRegion(songId, rangeHeader, extractUserDetails(authentication));
            String mimeType = songStreamingService.getSongMimeType(songId);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(region);

        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /// HELPERY

    // anonymousUser jest Stringiem, wiec zwroci null do pozniejszej obslugi
    private UserDetails extractUserDetails(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return (UserDetails) authentication.getPrincipal();
        } else return null;
    }

}
