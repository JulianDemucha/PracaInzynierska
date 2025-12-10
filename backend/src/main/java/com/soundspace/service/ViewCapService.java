package com.soundspace.service;

import com.soundspace.repository.SongRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;


// narazie glownie do obliczania capa po ktorym jeszcze wieksza ilosc wyswietlen nie ma znaczenia
@Slf4j
@Service
@RequiredArgsConstructor
public class ViewCapService {
    private final SongRepository songRepository;
    @Getter private volatile double viewCap = 1.0;


    @PostConstruct
    @Scheduled(cron = "0 0 3 * * *")
    public void updateViewCapCache() {
        List<Long> playCounts = songRepository.getAllViewCounts();

        long newCap = calculatePercentile90(playCounts);
        this.viewCap = (double) newCap;
        log.info("StatisticsService: cap wyswietlen (90th precentl) zostal zaktualizowany na: {}", newCap);
    }

    private long calculatePercentile90(List<Long> values) {
        if (values == null || values.isEmpty()) return 0;
        values.sort(Comparator.naturalOrder());
        int n = values.size();
        int index = (int) Math.ceil(0.90 * n) - 1;
        index = Math.max(0, Math.min(index, n - 1));
        return values.get(index);
    }

}