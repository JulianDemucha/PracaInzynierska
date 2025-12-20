package com.soundspace.cache.provider;

import com.soundspace.dto.SongStatslessDto;
import com.soundspace.service.song.SongCoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CachedSongsProvider {


    private final SongCoreService songCoreService;

    @Cacheable(value = "song", key = "#songId")
    public SongStatslessDto getSong(Long songId, UserDetails userDetails) {
        return songCoreService.getSong(songId, userDetails);
    }
}
