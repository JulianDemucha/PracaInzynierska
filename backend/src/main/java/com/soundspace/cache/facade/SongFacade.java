package com.soundspace.cache.facade;
import com.soundspace.cache.provider.CachedSongsProvider;
import com.soundspace.dto.SongDtoWithDetails;
import com.soundspace.service.song.SongEnrichmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SongFacade {

    private final CachedSongsProvider cachedSongsProvider;
    private final SongEnrichmentService songEnrichmentService;

    public SongDtoWithDetails getSong(Long songId, UserDetails userDetails) {
        return songEnrichmentService.addStatistics(cachedSongsProvider.getSong(songId, userDetails));
    }
}
