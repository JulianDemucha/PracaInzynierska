package com.soundspace.service.song;

import com.soundspace.dto.SongDto;
import com.soundspace.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
@Service
@RequiredArgsConstructor
public class SongStatisticsService {

    private final SongRepository songRepository;

    public List<SongDto> getTop10Liked(){
        return songRepository.findTopLikedSongsSinceCutoff(
                Instant.now().minusSeconds(60 * 60 * 24 * 7), //tydzien
                PageRequest.of(0, 10)
        ).stream().map(SongDto::toDto).toList();
    }
}
