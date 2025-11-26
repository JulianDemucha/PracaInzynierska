package com.soundspace.controller;

import com.soundspace.dto.AppUserDto;
import com.soundspace.dto.request.AppUserUpdateRequest;
import com.soundspace.service.AppUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class AppUserController {
    private final AppUserService appUserService;

    @GetMapping("/{userId}")
    public ResponseEntity<AppUserDto> getAppUser(@PathVariable Long userId) {
        return ResponseEntity.ok(appUserService.getAppUser(userId));
    }

    @GetMapping("/me")
    public ResponseEntity<AppUserDto> getAuthenticatedUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(appUserService.getAuthenticatedUser(authentication));
    }

    @PostMapping(value = "/me", consumes = "multipart/form-data")
    public ResponseEntity<?> updatePlayer(
            @ModelAttribute @Valid AppUserUpdateRequest playerUpdateRequest,
            Authentication authentication
    ) {
        //updatePlayer returns ResponseEntity<?>
        return appUserService.updateUser(playerUpdateRequest, authentication);
    }


    // todo: zrobic w serwisie logike i po stronie klienta tez
//    @DeleteMapping("/me")
//    public ResponseEntity<?> deleteUser(Authentication authentication) {
//        //deleteUser returns ResponseEntity<?>
//        return appUserService.deleteUser(authentication);
//    }
}
