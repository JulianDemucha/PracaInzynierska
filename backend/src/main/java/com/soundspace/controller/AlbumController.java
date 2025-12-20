package com.soundspace.controller;

import com.soundspace.dto.AlbumDto;
import com.soundspace.dto.SongDto;
import com.soundspace.dto.request.AlbumSongUploadRequest;
import com.soundspace.dto.request.AlbumCreateRequest;
import com.soundspace.dto.request.AlbumUpdateRequest;
import com.soundspace.service.AlbumService;
import com.soundspace.service.user.AppUserService;
import com.soundspace.service.song.SongUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    private final SongUploadService songUploadService;
    private final AppUserService appUserService;

    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public ResponseEntity<AlbumDto> createAlbum(@ModelAttribute @Valid AlbumCreateRequest albumCreateRequest,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        AlbumDto albumDto = albumService.createAlbum(albumCreateRequest, userDetails.getUsername());
        return ResponseEntity.created(URI.create("/api/albums/" + albumDto.id())).body(albumDto);
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable Long albumId,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        albumService.deleteAlbum(albumId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{albumId}")
    public ResponseEntity<AlbumDto> getAlbum(@PathVariable Long albumId,
                                             Authentication authentication) {
        return ResponseEntity.ok(albumService.getAlbumById(albumId, extractUserDetails(authentication)));
    }

    @PostMapping("/{albumId}/add")
    public ResponseEntity<SongDto> addSongToAlbum(@PathVariable Long albumId,
                                                  @ModelAttribute @Valid AlbumSongUploadRequest request,
                                                  @AuthenticationPrincipal UserDetails userDetails) {
        SongDto createdSong = songUploadService.upload(albumId, request,
                appUserService.getUserByEmail(userDetails.getUsername()));
        return ResponseEntity.created(URI.create("/api/albums/"+createdSong.id())).body(createdSong);
    }

    @DeleteMapping("/{albumId}/remove/{songId}")
    public ResponseEntity<Void> removeSongFromAlbum(@PathVariable Long albumId,
                                                    @PathVariable Long songId,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        albumService.removeAlbumSong(albumId, songId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{albumId}/songs")
    public ResponseEntity<List<SongDto>> getSongsByAlbumId(@PathVariable Long albumId,
                                                           Authentication authentication) {
        return ResponseEntity.ok(albumService.getSongs(albumId, extractUserDetails(authentication)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AlbumDto>> getUserAlbums(@PathVariable Long userId,
                                                        Authentication authentication) {
        return ResponseEntity.ok(albumService.findAllAlbumsByUserId(userId, extractUserDetails(authentication)));
    }

    @GetMapping("/genre/{genreName}")
    public ResponseEntity<Page<AlbumDto>> getPublicAlbumsByGenre(@PathVariable String genreName,
                                                                 Authentication authentication,
                                                                 @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(albumService.getPublicAlbumsByGenre(genreName, extractUserDetails(authentication), pageable));
    }

    @GetMapping
    public ResponseEntity<Page<AlbumDto>> getAllAlbums(Authentication authentication,
                                                       @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(albumService.getAllAlbums(extractUserDetails(authentication), pageable));
    }

    @PutMapping("/{albumId}")
    public ResponseEntity<AlbumDto> updateAlbum(@ModelAttribute @Valid AlbumUpdateRequest request,
                                                @PathVariable Long albumId,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(albumService.update(albumId, request, userDetails));
    }

    /// helpers

    private UserDetails extractUserDetails(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return (UserDetails) authentication.getPrincipal();
        } else return null;
    }


}
