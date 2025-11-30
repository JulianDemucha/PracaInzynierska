package com.soundspace.controller;

import com.soundspace.dto.AppUserDto;
import com.soundspace.dto.request.AppUserUpdateRequest;
import com.soundspace.service.AppUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    public ResponseEntity<AppUserDto> getAuthenticatedUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(appUserService.getAuthenticatedUser(userDetails));
    }

    @PutMapping(value = "/me", consumes = "multipart/form-data")
    public ResponseEntity<?> updateUser(
            @ModelAttribute @Valid AppUserUpdateRequest appUserUpdateRequest,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(appUserService.updateUser(appUserUpdateRequest, userDetails));
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal UserDetails userDetails) {
        String email = (userDetails != null) ? userDetails.getUsername() : null;
        appUserService.deleteUser(email);
        return ResponseEntity.noContent().build();
    }
}
