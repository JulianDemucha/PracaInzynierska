package com.soundspace.dto;

import lombok.Builder;

@Builder
public record CommentDto (
        Long id,
        Long creatorId,
        Long SongId, //jakas historie komentarzy moze zrobimy, jak nie to zbedne w sumie
        String content,
        String createdAt,
        String updatedAt
){}
