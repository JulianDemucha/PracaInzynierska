package com.soundspace.service;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpRange;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.soundspace.entity.Song;
import com.soundspace.repository.SongRepository;

@Service
@RequiredArgsConstructor
public class SongStreamingService {

    private final SongRepository songRepository;
    private final StorageService storageService;

    private static final long CHUNK_SIZE = 1024 * 1024; // 1MB

    @Transactional(readOnly = true)
    public ResourceRegion getSongRegion(Long songId, String rangeHeader, String requesterEmail) throws IOException {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new NoSuchElementException("Piosenka nie istnieje"));

        validateAccess(song, requesterEmail);

        Path path = storageService.resolvePath(song.getAudioStorageKey());
        if (!Files.exists(path)) {
            throw new NoSuchElementException("Plik fizyczny nie istnieje");
        }

        // obliczanie range
        UrlResource resource = new UrlResource(path.toUri());
        long contentLength = Files.size(path);

        if (rangeHeader == null || rangeHeader.isBlank()) {
            long rangeLength = Math.min(CHUNK_SIZE, contentLength);
            return new ResourceRegion(resource, 0, rangeLength);
        } else {
            HttpRange range = HttpRange.parseRanges(rangeHeader).get(0);
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            long rangeLength = Math.min(CHUNK_SIZE, end - start + 1);

            return new ResourceRegion(resource, start, rangeLength);
        }
    }

    private void validateAccess(Song song, String email) {
        if (Boolean.TRUE.equals(song.getPubliclyVisible())) {
            return;
        }

        if (!song.getAuthor().getEmail().equals(email)) {
            throw new AccessDeniedException("Brak dostępu");
        }
    }

    public String getSongMimeType(Long songId) {
        return songRepository.findById(songId)
                .map(Song::getMimeType)
                .orElse("application/octet-stream");
    }
}