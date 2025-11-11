package com.soundspace.controller;

import com.soundspace.dto.AppUserDto;
import com.soundspace.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class AppUserController {
    private final AppUserService appUserService;

    @GetMapping("/me")
    public ResponseEntity<AppUserDto> getAuthenticatedUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        //getAuthenticatedPlayer returns ResponseEntity<PlayerDto>
        return appUserService.getAuthenticatedUser(authentication);
    }
}
