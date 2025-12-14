package com.soundspace.dto.projection;

import java.time.Instant;

public interface PlaylistProjection {
    Long getId();
    String getTitle();
    Long getCreatorId();
    String getCreatorUsername();
    Boolean getPubliclyVisible();
    Instant getCreatedAt();
    Instant getUpdatedAt();
    Long getCoverStorageKeyId();
    Integer getSongsCount();
}
