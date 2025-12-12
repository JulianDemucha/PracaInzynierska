package com.soundspace.service.song;

import com.soundspace.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViewBufferingService {
    private final SongRepository songRepository;

    // (key: songId, value: viewCount)
    private final ConcurrentHashMap<Long, Long> viewBuffer = new ConcurrentHashMap<>();

    /**
     *  dzieki temu elegancko tymczasowo do viewBuffer zapisuja sie wszystkie wyswietlenia
     *  a pozniej z niego wyciaga zeby wywolac zapytania, czyli tymczasowo wszystko jest trzymane w RAM
     */
    public void bufferView(Long songId) {
        viewBuffer.merge(songId, 1L, Long::sum);
    }


    // co 20 sekund - ustawienie z application.yaml
    @Scheduled(fixedRateString = "${app.views.buffer-flush-rate-ms}") // ms
    @Transactional
    public void flushViewsToDatabase() {
        if (viewBuffer.isEmpty()) return;

        Map<Long, Long> snapshot = new HashMap<>(viewBuffer);
        // po wyjeciu czyscimy zeby w kolejnym odpaleniu metody znowu nie dodawac tego samego
        snapshot.keySet().forEach(viewBuffer::remove);

        // i elegancko zamiast po x zapytan (na kazde oddzielne wyswietlenie) dla kadzej piosenki, leci jedno na kazda piosenke
        for (Map.Entry<Long, Long> entry : snapshot.entrySet()) {
            Long songId = entry.getKey();
            Long countToAdd = entry.getValue();

            songRepository.incrementViewCountBy(songId, countToAdd);
        }

        log.info("Zapisano wyświetlenia dla {} piosenek.", snapshot.size());
    }
}