package com.soundspace.dto.request;

import lombok.Builder;

@Builder
public record AppUserUpdateRequest (
        String username,
        String email,
        String password,
        String bio,
        String sex
){}
