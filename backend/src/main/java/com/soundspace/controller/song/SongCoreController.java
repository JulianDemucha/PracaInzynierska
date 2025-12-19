package com.soundspace.controller.song;
import com.soundspace.dto.SongDto;
import com.soundspace.dto.request.SongUpdateRequest;
import com.soundspace.dto.request.SongUploadRequest;
import com.soundspace.service.song.SongCoreService;
import com.soundspace.service.song.SongUploadService;
import com.soundspace.service.user.AppUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@Validated
public class SongCoreController {

    private final SongCoreService songCoreService;
    private final SongUploadService songUploadService;
    private final AppUserService appUserService;


    // totalnie core metody

    @GetMapping("/{songId}")
    public ResponseEntity<SongDto> getSongById(@NotNull @PathVariable Long songId, Authentication authentication) {
        return ResponseEntity.ok(songCoreService.getSong(songId, extractUserDetails(authentication)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SongDto>> getSongsByUserId(@PathVariable Long userId,
                                                          Authentication authentication) {
        return ResponseEntity.ok(songCoreService.getSongsByUserId(userId, extractUserDetails(authentication)));
    }

    @GetMapping
    public ResponseEntity<List<SongDto>> getAllSongs(Authentication authentication) {
        return ResponseEntity.ok(songCoreService.getAllSongs(extractUserDetails(authentication)));
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

    @PutMapping("/{songId}")
    public ResponseEntity<SongDto> updateSongById(@ModelAttribute @Valid SongUpdateRequest request,
                                                  @PathVariable Long songId,
                                                  Authentication authentication) {
        return ResponseEntity.ok(songCoreService.update(songId, request, extractUserDetails(authentication)));
    }

    @DeleteMapping("/{songId}")
    public ResponseEntity<Void> deleteSongById(@PathVariable Long songId,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        String email = (userDetails != null) ? userDetails.getUsername() : null;
        songCoreService.deleteSongById(songId, email);
        return ResponseEntity.noContent().build(); //402
    }


    // specyficzne nie-core endpointy

    @GetMapping("/genre/{genreName}")
    public ResponseEntity<Page<SongDto>> getSongsByGenre(@PageableDefault Pageable pageable,
                                                      @PathVariable String genreName,
                                                      Authentication authentication) {
        return ResponseEntity.ok(
                songCoreService.getSongsByGenre(genreName,
                extractUserDetails(authentication),
                        pageable
                ));
    }

    @GetMapping("/favourites")
    public ResponseEntity<Page<SongDto>> getFavouriteSongs(@PageableDefault Pageable pageable,
                                                           @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(songCoreService.getFavouriteSongs(userDetails, pageable));
    }


    // HELPERY

    // anonymousUser jest Stringiem, wiec zwroci null do pozniejszej obslugi
    private UserDetails extractUserDetails(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return (UserDetails) authentication.getPrincipal();
        } else return null;
    }

}
