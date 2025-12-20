package com.soundspace.controller.song;

import com.soundspace.dto.SongBaseDto;
import com.soundspace.service.song.SongStatisticsService;
import com.soundspace.service.song.ViewService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@Validated
public class SongStatisticsController {
    private final SongStatisticsService songStatisticsService;
    private final ViewService viewService;

    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
    };


    @PostMapping("/{songId}/registerView")
    public ResponseEntity<Void> registerView(@PathVariable Long songId,
                                             @AuthenticationPrincipal UserDetails userDetails,
                                             HttpServletRequest request
    ) {
        String clientIp = extractClientIp(request);
        boolean isNewViewRegistered = viewService.registerView(songId, userDetails, clientIp);

        return ( isNewViewRegistered ? ResponseEntity.ok() : ResponseEntity.noContent() ).build();
    }

    @GetMapping("/top/trending")
    public ResponseEntity<Page<SongBaseDto>> getTrendingSongs(@PageableDefault Pageable pageable) {
        return ResponseEntity.ok(songStatisticsService.getTrendingSongs(pageable));
    }

    @GetMapping("/top/liked")
    public ResponseEntity<Page<SongBaseDto>> getTopLikedSongs(@PageableDefault Pageable pageable) {
        return ResponseEntity.ok(songStatisticsService.getTopLiked(pageable));
    }

    @GetMapping("/top/viewed")
    public ResponseEntity<Page<SongBaseDto>> getTopViewedSongs(@PageableDefault Pageable pageable) {
        return ResponseEntity.ok(songStatisticsService.getTopViewed(pageable));
    }

    /// helpery


    private String extractClientIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);

            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
