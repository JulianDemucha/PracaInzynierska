package com.soundspace.controller;

import com.soundspace.dto.AlbumDto;
import com.soundspace.dto.SongDto;
import com.soundspace.dto.request.CreateAlbumRequest;
import com.soundspace.service.AlbumService;
import com.soundspace.service.SongCoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumService albumService;
    private final SongCoreService songCoreService;

    public AlbumController(AlbumService albumService, SongCoreService songCoreService) {
        this.albumService = albumService;
        this.songCoreService = songCoreService;
    }

    @PostMapping("/create")
    public ResponseEntity<AlbumDto> createAlbum(@RequestBody CreateAlbumRequest createAlbumRequest) {
        AlbumDto albumDto = albumService.createAlbum(createAlbumRequest);
        return ResponseEntity.created(URI.create("/api/albums/" + albumDto.id())).body(albumDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlbumDto> getAlbum(@PathVariable Long id) {
        return ResponseEntity.ok(albumService.getAlbum(id));
    }

    @PostMapping("/{albumId}/add/{songId}")
    public ResponseEntity<SongDto> addSong(@PathVariable Long albumId, @PathVariable Long songId,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        String email = (userDetails != null) ? userDetails.getUsername() : null;
        return ResponseEntity.ok(albumService.addSongToAlbum(albumId, songId, email));
    }

    @GetMapping("/{albumId}/songs")
    public ResponseEntity<List<SongDto>> getSongsByAlbumId(@PathVariable Long albumId,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        String email = (userDetails != null) ? userDetails.getUsername() : null;
        return ResponseEntity.ok(songCoreService.getSongsByAlbumId(albumId, email));
    }

}
