package com.soundspace.service.song;

import com.soundspace.entity.Song;
import com.soundspace.entity.StorageKey;
import com.soundspace.repository.SongRepository;
import com.soundspace.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpRange;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SongStreamingService {

    private final SongRepository songRepository;
    private final StorageService storageService;

    private static final long CHUNK_SIZE = 1024 * 1024; // 1MB

    @Transactional(readOnly = true)
    public ResourceRegion getSongRegion(Long songId, String rangeHeader, UserDetails userDetails) throws IOException {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new NoSuchElementException("Piosenka nie istnieje"));

        validateAccess(song, userDetails);

        StorageKey audioKey = song.getAudioStorageKey();
        if (audioKey == null || audioKey.getKeyStr() == null || audioKey.getKeyStr().isBlank()) {
            throw new NoSuchElementException("Brak przypisanego pliku audio dla piosenki");
        }

        Path path = storageService.resolvePath(audioKey.getKeyStr());
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
            List<HttpRange> ranges = HttpRange.parseRanges(rangeHeader);
            if (ranges.isEmpty()) {
                long rangeLength = Math.min(CHUNK_SIZE, contentLength);
                return new ResourceRegion(resource, 0, rangeLength);
            }
            HttpRange range = ranges.getFirst();
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            long rangeLength = Math.min(CHUNK_SIZE, end - start + 1);
            return new ResourceRegion(resource, start, rangeLength);
        }
    }

    private void validateAccess(Song song, UserDetails userDetails) {
        if (Boolean.TRUE.equals(song.getPubliclyVisible())) {
            return;
        }

        if (userDetails == null) throw new AccessDeniedException("Brak dostępu do piosenki");

        String authorEmail = song.getAuthor().getEmail();

        if (!authorEmail.equals(userDetails.getUsername()) ||
                userDetails.getAuthorities().stream().anyMatch(
                        authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Brak dostępu");
        }
    }

    public String getSongMimeType(Long songId) {
        return songRepository.findById(songId)
                .map(s -> s.getAudioStorageKey().getMimeType())
                .orElse("application/octet-stream"); //fallback
    }
}
