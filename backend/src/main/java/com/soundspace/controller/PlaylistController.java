package com.soundspace.controller;

import com.soundspace.dto.PlaylistDto;
import com.soundspace.dto.PlaylistSongViewDto;
import com.soundspace.dto.request.CreatePlaylistRequest;
import com.soundspace.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {
    private final PlaylistService playlistService;

    @GetMapping("/{playlistId}")
    public ResponseEntity<PlaylistDto> getPlaylist(@PathVariable Long playlistId,
                                                   @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(playlistService.getById(playlistId, userDetails));
    }

    @PostMapping("/create")
    public ResponseEntity<PlaylistDto> createPlaylist(@ModelAttribute CreatePlaylistRequest request,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(playlistService.create(request, userDetails));
    }

    @PostMapping("/{playlistId}/add/{songId}")
    public ResponseEntity<PlaylistSongViewDto> addSongToPlaylist(@PathVariable Long playlistId,
                                                                 @PathVariable Long songId,
                                                                 @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(playlistService.addSong(playlistId, songId, userDetails));
    }

    @GetMapping("/{playlistId}/songs")
    public ResponseEntity<List<PlaylistSongViewDto>> getPlaylistSongs(@PathVariable Long playlistId,
                                                                      @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(playlistService.getSongs(playlistId, userDetails));
    }

    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Void> delete(@PathVariable Long playlistId,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        playlistService.delete(playlistId, userDetails);
        return ResponseEntity.noContent().build();
    }
}
