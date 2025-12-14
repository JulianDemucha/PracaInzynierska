package com.soundspace.dto.projection;

import java.time.Instant;
import java.util.List;

public interface AlbumProjection {
    Long getId();
    String getTitle();
    String getDescription();
    Long getAuthorId();
    String getAuthorLogin();
    Boolean getPubliclyVisible();
    Instant getCreatedAt();
    Long getCoverStorageKeyId();
    String getGenresStr(); // Tutaj wpadnie "ROCK,POP"

    default List<String> getGenres() {
        if (getGenresStr() == null || getGenresStr().isBlank()) return List.of();
        return List.of(getGenresStr().split(","));
    }
}