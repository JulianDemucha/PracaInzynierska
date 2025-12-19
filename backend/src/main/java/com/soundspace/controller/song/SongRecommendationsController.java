package com.soundspace.controller.song;

import com.soundspace.dto.SongDto;
import com.soundspace.service.song.RecommendationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/songs/recommendations")
@RequiredArgsConstructor
@Validated
public class SongRecommendationsController {
    private final RecommendationsService recommendationsService;

    @GetMapping
    public ResponseEntity<Page<SongDto>> getRecommendations(@PageableDefault Pageable pageable,
                                                            @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(recommendationsService.getRecommendations(userDetails, pageable));
    }
}
