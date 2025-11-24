package com.soundspace.controller;

import com.soundspace.dto.AppUserDto;
import com.soundspace.dto.request.AuthenticationRequest;
import com.soundspace.dto.RefreshTokenCookieDto;
import com.soundspace.dto.request.RegisterRequest;
import com.soundspace.entity.AppUser;
import com.soundspace.service.AppUserService;
import com.soundspace.service.AuthenticationService;
import com.soundspace.service.CookieService;
import com.soundspace.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final CookieService cookieService;
    private final AppUserService appUserService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest registerRequest,
            HttpServletResponse response
    ) {
        String jwt = authenticationService.register(registerRequest);
        String email = registerRequest.getEmail();

        cookieService.setJwtAndRefreshCookie(jwt,
                refreshTokenService.createRefreshToken(email).getRefreshToken(), response,
                60 * 60 * 24 /* 24h */, 60 * 60 * 24 * 30/* 30D */);

        AppUserDto appUserDto = AppUserDto.toDto(appUserService.getUserByEmail(email));
        URI location = URI.create("/api/users/" + appUserDto.id());
        return ResponseEntity.created(location).body(appUserDto);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(
            @RequestBody AuthenticationRequest authenticationRequest,
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        String jwt = authenticationService.authenticate(authenticationRequest);
        String email = authenticationRequest.getEmail();
        cookieService.setJwtAndRefreshCookie(jwt,
                refreshTokenService.createRefreshToken(email).getRefreshToken(), response,
                60 * 15 /* 15 min */, 60 * 60 * 24 * 30/* 30 D */);

        // revoke old if present
        if(refreshToken != null) {
            refreshTokenService.revokeRefreshToken(refreshToken);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("No refresh token found");
        }

        RefreshTokenCookieDto refreshTokenCookieDto =
                refreshTokenService.createNewRefreshToken(refreshToken);
        refreshTokenService.revokeRefreshToken(refreshToken);


        cookieService.setJwtAndRefreshCookie(refreshTokenCookieDto, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        Optional<Cookie> refreshCookie = Optional.empty();
        if (request.getCookies() != null) {
            refreshCookie = Arrays.stream(request.getCookies())
                    .filter(c -> "refreshToken".equals(c.getName()))
                    .findFirst();
        }

        refreshCookie.ifPresent(cookie -> {
            String token = cookie.getValue();
            refreshTokenService.revokeRefreshToken(token);
        });

        cookieService.setJwtAndRefreshCookie("","",response,0, 0);


        SecurityContextHolder.clearContext();


        return ResponseEntity.noContent().build();
    }
}
