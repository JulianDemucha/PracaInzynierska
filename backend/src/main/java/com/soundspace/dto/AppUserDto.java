package com.soundspace.dto;

import lombok.Builder;

@Builder
public record AppUserDto (
        Long id,
        String username,
        String sex,
        String role,
        String createdAt,
        String authProvider,
        boolean emailVerified,
        String bio,
        List<CommentDto> comments
){}
