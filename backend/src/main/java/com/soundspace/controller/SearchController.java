package com.soundspace.controller;

import com.soundspace.dto.*;
import com.soundspace.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/songs/search")
    public ResponseEntity<Page<SongBaseDto>> searchSongs(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication
    ) {
        return ResponseEntity.ok(searchService.searchSongs(query, pageable, extractUserDetails(authentication)));
    }

    @GetMapping("/albums/search")
    public ResponseEntity<Page<AlbumDto>> searchAlbums(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication
    ) {
        return ResponseEntity.ok(searchService.searchAlbums(query, pageable, extractUserDetails(authentication)));
    }

    @GetMapping("/playlists/search")
    public ResponseEntity<Page<PlaylistDto>> searchPlaylists(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication
    ) {
        return ResponseEntity.ok(searchService.searchPlaylists(query, pageable, extractUserDetails(authentication)));
    }

    @GetMapping("/users/search")
    public ResponseEntity<Page<AppUserDto>> searchusers(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(searchService.searchUsers(query, pageable));
    }


    //todo zrobic search do appUserow

    // HELPERY

    private UserDetails extractUserDetails(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return (UserDetails) authentication.getPrincipal();
        } else return null;
    }
}
