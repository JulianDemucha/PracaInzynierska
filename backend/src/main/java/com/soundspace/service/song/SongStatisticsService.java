package com.soundspace.service.song;

import com.soundspace.dto.SongDto;
import com.soundspace.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
@Service
@RequiredArgsConstructor
public class SongStatisticsService {

    private final SongRepository songRepository;

    public Page<SongDto> getTopLiked(Pageable pageable) {
        return songRepository.findTopLikedSongsSinceCutoff(
                Instant.now().minusSeconds(60 * 60 * 24 * 7), //tydzien
                pageable
        ).map(SongDto::toDto);
    }

    public Page<SongDto> getTopViewed(Pageable pageable) {
        return songRepository.findTopViewedSongs(
                pageable
        ).map(SongDto::toDto);
    }
}
