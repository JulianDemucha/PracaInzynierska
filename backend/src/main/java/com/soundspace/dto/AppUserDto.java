package com.soundspace.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record AppUserDto (
        Long id,
        String username,
        String email,
        String sex,
        String role,
        String createdAt,
        String authProvider,
        boolean emailVerified,
        String bio
//        List<Long> commentsIds
){}
