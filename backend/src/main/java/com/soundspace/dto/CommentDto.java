package com.soundspace.dto;

import lombok.Builder;

@Builder
public record CommentDto (
        Long id,
        Long creatorId,
        Long SongId,
        String content,
        String createdAt,
        String updatedAt
){}
