package com.soundspace.security.dto;

import lombok.Builder;

@Builder
public record AppUserUpdateRequest (
        String username,
        String email,
        String password,
        String bio,
        String sex
){}
