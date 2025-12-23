package com.soundspace.util;

import com.soundspace.dto.SongDtoWithDetails;
import com.soundspace.dto.SongStatslessDto;
import com.soundspace.entity.SongStatistics;
import com.soundspace.repository.SongStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SongAssembler {

    private final SongStatisticsRepository songStatisticsRepository;

    /**
     * Przyjmuje listę DTO (np. z Cache'a) i zwraca nową listę
     * ze zaktualizowanymi licznikami lajków i wyświetleń pobranymi prosto z bazy.
     * Piosenki zachowują swoją kolejność
     */
    @Transactional(readOnly = true)
    public List<SongDtoWithDetails> addStatistics(List<SongStatslessDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> songIds = dtos.stream()
                .map(SongStatslessDto::id)
                .toList();

        List<SongStatistics> statistics = songStatisticsRepository.findAllById(songIds);

        Map<Long, SongStatistics> statsMap = statistics.stream()
                .collect(Collectors.toMap(SongStatistics::getId, Function.identity()));

        return dtos.stream()
                .map(dto -> {
                    SongStatistics stats = statsMap.get(dto.id());
                    return dto.withStatistics(stats);
                })
                .toList();
    }

    /**
     * Wersja dla pojedynczej piosenki
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "song-stats", key = "#cachedDto.id()")
    public SongDtoWithDetails addStatistics(SongStatslessDto cachedDto) {
        if (cachedDto == null) return null;

        return songStatisticsRepository.findById(cachedDto.id())
                .map(cachedDto::withStatistics)
                .orElse(cachedDto.withStatistics(null));
    }
}