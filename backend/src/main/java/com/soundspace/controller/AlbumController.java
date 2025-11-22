package com.soundspace.controller;

import com.soundspace.dto.AlbumDto;
import com.soundspace.dto.SongDto;
import com.soundspace.dto.request.CreateAlbumRequest;
import com.soundspace.service.AlbumService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumService albumService;

    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
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
        return ResponseEntity.ok(albumService.addSongToAlbum(albumId, songId));
    }

}
