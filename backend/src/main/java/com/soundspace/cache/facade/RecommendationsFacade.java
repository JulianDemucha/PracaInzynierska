package com.soundspace.cache.facade;
import com.soundspace.dto.SongDto;
import com.soundspace.repository.SongRepository;
import com.soundspace.service.song.CachedRecommendationsProvider;
import com.soundspace.util.SongPaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationsFacade {

    private final CachedRecommendationsProvider cachedProvider;
    private final SongRepository songRepo; // Do fallbacku na global top

    public Page<SongDto> getRecommendations(UserDetails userDetails, Pageable pageable) {
        if (userDetails == null) {
            return getGlobalTopSongs(pageable);
        }

        List<SongDto> recommendedSongs = cachedProvider.getRecommendations(userDetails);

        if (recommendedSongs.isEmpty()) {
            return getGlobalTopSongs(pageable);
        }

        return SongPaginationUtil.toPage(recommendedSongs, pageable);
    }

    private Page<SongDto> getGlobalTopSongs(Pageable pageable) {
        return songRepo.findTrendingSongs(
                Instant.now().minusSeconds(60 * 60 * 24 * 7),
                pageable,
                3
        ).map(SongDto::toDto);
    }
}