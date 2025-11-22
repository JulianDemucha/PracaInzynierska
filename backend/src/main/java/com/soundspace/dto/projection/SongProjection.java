package com.soundspace.dto.projection;

import lombok.Value;

import java.time.Instant;
import java.util.List;

public interface SongProjection {
    Long getId();
    String getTitle();
    String getAuthorLogin();
    Long getAlbumId();
    String getGenresStr();
    boolean getPubliclyVisible();
    Instant getCreatedAt();
    String getCoverStorageKey();

    default List<String> getGenres() {
        String raw = getGenresStr();
        if (raw == null) return List.of();
        return List.of(raw.split(","));
    }
}
