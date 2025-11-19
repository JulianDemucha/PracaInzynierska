package com.soundspace.dto;

import lombok.Builder;

@Builder
public record SongDto(
        Long id,
        String title,
        String authorUsername,
        String genre,
        boolean publiclyVisible,
        String createdAt
){}
