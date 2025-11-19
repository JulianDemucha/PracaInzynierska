package com.soundspace.dto;

import lombok.Builder;

@Builder
public record SongDto(
        String title,
        String authorUsername,
        String genre,
        boolean publiclyVisible,
        String createdAt
){}
