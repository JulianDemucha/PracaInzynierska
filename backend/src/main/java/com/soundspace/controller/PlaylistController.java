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

import java.net.URI;
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
        PlaylistDto createdPlaylist = playlistService.create(request, userDetails);
        return ResponseEntity.created(URI.create("/api/playlists/"+createdPlaylist.id())).body(createdPlaylist);
    }

    @PostMapping("/{playlistId}/add/{songId}")
    public ResponseEntity<PlaylistSongViewDto> addSongToPlaylist(@PathVariable Long playlistId,
                                                                 @PathVariable Long songId,
                                                                 @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity
                .created(URI.create("/api/playlists/songs"))
                .body(playlistService.addSong(playlistId, songId, userDetails));
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

    @DeleteMapping("/{playlistId}/remove/{songId}")
    public ResponseEntity<Void> removeSong(@PathVariable Long playlistId,
                                           @PathVariable Long songId,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        playlistService.removeSong(playlistId, songId, userDetails);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{appUserId}")
    public ResponseEntity<List<PlaylistDto>> getAllUserPlaylists(@PathVariable Long appUserId,
                                                                 @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(playlistService.getAllByUserId(appUserId, userDetails));
    }

    @PutMapping("/{playlistId}/changeSongPosition/{songId}/{newPosition}")
    public ResponseEntity<PlaylistSongViewDto> changeSongPosition(@PathVariable Long playlistId,
                                                                  @PathVariable Long songId,
                                                                  @PathVariable Integer newPosition,
                                                                  @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(playlistService.changeSongPosition(playlistId, songId, newPosition, userDetails));
    }

    @GetMapping("/")
    public ResponseEntity<List<PlaylistDto>> getAllPlaylists(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(playlistService.getAllPlaylists(userDetails));
    }
}
