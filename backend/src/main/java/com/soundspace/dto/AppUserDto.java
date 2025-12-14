package com.soundspace.dto;

import com.soundspace.dto.projection.AppUserProjection;
import com.soundspace.entity.AppUser;
import lombok.Builder;

@Builder
public record AppUserDto (
        Long id,
        String username,
        String email,
        String sex,
        String role,
        String createdAt,
        String authProvider,
        Boolean emailVerified,
        String bio,
        Long avatarStorageKeyId
){

    public static AppUserDto toDto(AppUser appUser) {

        return new AppUserDto(
                appUser.getId(),
                appUser.getLogin(),
                appUser.getEmail(),
                appUser.getSex().toString(),
                appUser.getRole().toString(),
                appUser.getCreatedAt().toString(),
                appUser.getAuthProvider().toString(),
                appUser.isEmailVerified(),
                appUser.getBio(),
                appUser.getAvatarStorageKey().getId()
                );
    }

    public static AppUserDto toDto(AppUserProjection p) {
        return new AppUserDto(
                p.getId(),
                p.getLogin(),
                p.getEmail(),
                p.getSex().toString(),
                p.getRole().toString(),
                p.getCreatedAt().toString(),
                p.getAuthProvider().toString(),
                p.getEmailVerified(),
                p.getBio(),
                p.getAvatarStorageKeyId()
        );
    }
}
