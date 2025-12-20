package com.soundspace.dto.projection;
import java.time.Instant;
import java.util.List;

// do pobierania wielu songow do ktorych potrzeba
public interface SongStatslessProjection {
    Long getId();
    String getTitle();
    Long getAuthorId();
    String getAuthorUsername();
    Long getAlbumId();
    String getGenresStr();
    boolean getPubliclyVisible();
    Instant getCreatedAt();
    Long getCoverStorageKeyId();

    default List<String> getGenres() {
        String raw = getGenresStr();
        if (raw == null) return List.of();
        return List.of(raw.split(","));
    }
}
