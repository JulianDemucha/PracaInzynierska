package com.soundspace.controller.song;

import com.soundspace.dto.SongDto;
import com.soundspace.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@Validated
public class SongStatisticsController {
    private final SongCoreService songCoreService;
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

    @GetMapping("/top10")
    public ResponseEntity<List<SongDto>> getTop10LikedSongs() {
        return ResponseEntity.ok(songCoreService.getTop10Liked());
    }

    //todo zrobic top10viewed i poprawic top10liked zeby zwracalo page i przyjmowalo wiadomo size i page

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
