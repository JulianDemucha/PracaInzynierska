package com.soundspace.dto.mapper;

import com.soundspace.dto.AppUserDto;
import com.soundspace.entity.AppUser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AppUserMapper {

    public AppUserDto toDto(AppUser appUser){
        return AppUserDto.builder()
                .id(appUser.getId())
                .username(appUser.getUsername())
                .sex(appUser.getSex().toString())
                .role(appUser.getRole().toString())
                .createdAt(appUser.getCreatedAt().toString())
                /*
                createdAt:
                example toString output: 2025-07-24T15:30:45
                24=day T=separator 15=hour 30=minutes 45=seconds
                 */
                .authProvider(appUser.getAuthProvider().toString())
                .emailVerified(appUser.isEmailVerified())
                .bio(appUser.getBio())
                .build();
    }

    public List<AppUserDto> toDto(List<AppUser> appUsers){
        List<AppUserDto> appUserDtos = new ArrayList<>();
        for(AppUser appUser : appUsers){
            appUserDtos.add(toDto(appUser));
        }
        return appUserDtos;
    }
}
