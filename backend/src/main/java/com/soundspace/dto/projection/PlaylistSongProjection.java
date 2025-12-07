package com.soundspace.dto.projection;

import java.time.Instant;
import java.util.List;

public interface PlaylistSongProjection {
    Long getId();
    String getTitle();
    Long getAuthorId();
    String getAuthorUsername();
    Long getAlbumId();
    String getGenresStr();
    Boolean getPubliclyVisible();
    Instant getCreatedAt();
    Long getCoverStorageKeyId();
    Integer getPosition();
    Integer getLikesCount();
    Integer getDislikesCount();
    Long getViewCount();

    default List<String> getGenres() {
        String raw = getGenresStr();
        if (raw == null) return List.of();
        return List.of(raw.split(","));
    }
}