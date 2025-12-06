package com.soundspace.controller;

import com.soundspace.dto.SongDto;
import com.soundspace.dto.request.SongUpdateRequest;
import com.soundspace.dto.request.SongUploadRequest;
import com.soundspace.enums.ReactionType;
import com.soundspace.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.net.URI;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@Validated
public class SongController {

    private final SongUploadService songUploadService;
    private final AppUserService appUserService;
    private final SongStreamingService songStreamingService;
    private final SongCoreService songCoreService;
    private final ReactionService reactionService;

    @GetMapping("/{songId}")
    public ResponseEntity<SongDto> getSongById(@NotNull @PathVariable Long songId, Authentication authentication) {
        return ResponseEntity.ok(songCoreService.getSong(songId, extractUserDetails(authentication)));
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<SongDto> upload(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute @Valid SongUploadRequest request) {

        SongDto result = songUploadService.upload(
                request,
                appUserService.getUserByEmail(userDetails.getUsername())
        );
        URI location = URI.create("/api/songs/" + result.id());
        return ResponseEntity.created(location).body(result);
    }

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

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SongDto>> getSongsByUserId(@PathVariable Long userId,
                                                          Authentication authentication) {
        return ResponseEntity.ok(songCoreService.getSongsByUserId(userId, extractUserDetails(authentication)));
    }

    @DeleteMapping("/{songId}")
    public ResponseEntity<Void> deleteSongById(@PathVariable Long songId,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        String email = (userDetails != null) ? userDetails.getUsername() : null;
        songCoreService.deleteSongById(songId, email);
        return ResponseEntity.noContent().build(); //402
    }

    @GetMapping("/genre/{genreName}")
    public ResponseEntity<List<SongDto>> getSongsByGenre(@PathVariable String genreName,
                                                         Authentication authentication) {
        return ResponseEntity.ok(songCoreService.getSongsByGenre(genreName, extractUserDetails(authentication)));
    }

    @GetMapping
    public ResponseEntity<List<SongDto>> getAllSongs(Authentication authentication) {
        return ResponseEntity.ok(songCoreService.getAllSongs(extractUserDetails(authentication)));
    }

    @PutMapping("/{songId}")
    public ResponseEntity<SongDto> updateSongById(@ModelAttribute @Valid SongUpdateRequest request,
                                                  @PathVariable Long songId,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(songCoreService.update(songId, request, userDetails));
    }

    /// SEKCJA REAKCJI

    @PostMapping("/{songId}/like")
    public ResponseEntity<Void> likeSong(@PathVariable Long songId, @AuthenticationPrincipal UserDetails userDetails) {
        reactionService.addReaction(songId, ReactionType.LIKE, userDetails);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{songId}/dislike")
    public ResponseEntity<Void> dislikeSong(@PathVariable Long songId, @AuthenticationPrincipal UserDetails userDetails) {
        reactionService.addReaction(songId, ReactionType.DISLIKE, userDetails);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{songId}/favourite")
    public ResponseEntity<Void> favouriteSong(@PathVariable Long songId, @AuthenticationPrincipal UserDetails userDetails) {
        reactionService.addReaction(songId, ReactionType.FAVOURITE, userDetails);
        return ResponseEntity.ok().build();
    }


    /// HELPERY

    private UserDetails extractUserDetails(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return (UserDetails) authentication.getPrincipal();
        } else return null;
    }
}
