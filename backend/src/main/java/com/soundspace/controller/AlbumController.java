package com.soundspace.controller;

import com.soundspace.dto.AlbumDto;
import com.soundspace.dto.SongDto;
import com.soundspace.dto.request.CreateAlbumRequest;
import com.soundspace.service.AlbumService;
import com.soundspace.service.SongCoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;
    private final SongCoreService songCoreService;

    @PostMapping("/create")
    public ResponseEntity<AlbumDto> createAlbum(@RequestBody CreateAlbumRequest createAlbumRequest,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        AlbumDto albumDto = albumService.createAlbum(createAlbumRequest, extractUserEmail(userDetails));
        return ResponseEntity.created(URI.create("/api/albums/" + albumDto.id())).body(albumDto);
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable Long albumId,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        albumService.deleteAlbum(albumId, extractUserEmail(userDetails));
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{albumId}")
    public ResponseEntity<AlbumDto> getAlbum(@PathVariable Long albumId,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(albumService.getAlbumById(albumId, extractUserEmail(userDetails)));
    }

    @PostMapping("/{albumId}/add/{songId}")
    public ResponseEntity<SongDto> addSongToAlbum(@PathVariable Long albumId, @PathVariable Long songId,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(albumService.addSongToAlbum(albumId, songId, extractUserEmail(userDetails)));
    }

    @DeleteMapping("/{albumId}/remove/{songId}")
    public ResponseEntity<Void> removeSongFromAlbum(@PathVariable Long albumId,
                                                    @PathVariable Long songId,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        albumService.removeSongFromAlbum(albumId, songId, extractUserEmail(userDetails));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{albumId}/songs")
    public ResponseEntity<List<SongDto>> getSongsByAlbumId(@PathVariable Long albumId,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(songCoreService.getSongsByAlbumId(albumId, extractUserEmail(userDetails)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AlbumDto>> getUserAlbums(@PathVariable Long userId,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(albumService.findAllAlbumsByUserId(userId, extractUserEmail(userDetails)));
    }

    @GetMapping("/genre/{genreName}")
    public ResponseEntity<List<AlbumDto>> getPublicAlbumsByGenre(@PathVariable String genreName) {
        return ResponseEntity.ok(albumService.getPublicAlbumsByGenre(genreName))  ;
    }

    private String extractUserEmail(UserDetails userDetails) {
        return (userDetails != null) ? userDetails.getUsername() : null;
    }


}
