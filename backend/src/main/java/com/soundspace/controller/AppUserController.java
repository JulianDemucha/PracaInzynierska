package com.soundspace.controller;

import com.soundspace.dto.AppUserDto;
import com.soundspace.security.dto.AppUserUpdateRequest;
import com.soundspace.service.AppUserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/me")
    public ResponseEntity<?> updatePlayer(
            @NonNull @RequestBody AppUserUpdateRequest playerUpdateRequest,
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
