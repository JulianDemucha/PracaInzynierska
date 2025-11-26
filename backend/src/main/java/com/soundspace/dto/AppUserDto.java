package com.soundspace.dto;

import com.soundspace.entity.AppUser;
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
        Boolean emailVerified,
        String bio,
        Long avatarStorageKeyId
//        List<Long> commentsIds
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
}
