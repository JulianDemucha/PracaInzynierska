package com.soundspace.controller;
import com.soundspace.dto.SongDto;
import com.soundspace.dto.request.SongUploadRequest;
import com.soundspace.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
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

    @GetMapping("/{id}")
    public ResponseEntity<SongDto> getSongById(@NotNull @PathVariable Long id) {
        return ResponseEntity.ok(songCoreService.getSongDtoById(id));
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

    @GetMapping("/stream/{id}")
    public ResponseEntity<ResourceRegion> streamSong(
            @PathVariable Long id,
            @RequestHeader(value = "Range", required = false) String rangeHeader,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            String email = (userDetails != null) ? userDetails.getUsername() : null;

            ResourceRegion region = songStreamingService.getSongRegion(id, rangeHeader, email);
            String mimeType = songStreamingService.getSongMimeType(id);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(region);

        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<SongDto>> getSongsByUserId(@PathVariable Long id,
                                                          @AuthenticationPrincipal UserDetails userDetails) {
        String email = (userDetails != null) ? userDetails.getUsername() : null;
        return ResponseEntity.ok(songCoreService.getSongsByUserId(id, email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSongById(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        String email = (userDetails != null) ? userDetails.getUsername() : null;
        songCoreService.deleteSongById(id, email);
        return ResponseEntity.noContent().build(); //402
    }

    @GetMapping("/genre/{genreName}")
    public ResponseEntity<List<SongDto>> getSongsByGenre(@PathVariable String genreName) {
        return ResponseEntity.ok(songCoreService.getSongsByGenre(genreName));
    }

}
