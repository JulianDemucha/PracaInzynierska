package com.soundspace.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SongDto(
        Long id,
        String title,
        String authorUsername,
        Long albumId,
        List<String> genres,
        boolean publiclyVisible,
        String createdAt,
        String audioFileUrl,
        String coverFileUrl
){}
