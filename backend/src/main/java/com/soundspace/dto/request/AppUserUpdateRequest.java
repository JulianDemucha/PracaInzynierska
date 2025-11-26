package com.soundspace.dto.request;

import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
public record AppUserUpdateRequest (
        String username,
        String email,
        String password,
        String bio,
        String sex,
        MultipartFile avatarImageFile
){}
