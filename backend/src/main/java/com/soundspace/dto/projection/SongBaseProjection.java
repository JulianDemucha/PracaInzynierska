package com.soundspace.dto.projection;

public interface SongBaseProjection {
    Long getId();
    String getTitle();
    Long getAuthorId();
    String getAuthorUsername();
    Long getCoverStorageKeyId();
    String getCreatedAt();
}