package com.soundspace.controller;

import com.soundspace.security.dto.AuthenticationRequest;
import com.soundspace.security.dto.RefreshTokenCookieDto;
import com.soundspace.security.dto.RegisterRequest;
import com.soundspace.service.AuthenticationService;
import com.soundspace.service.CookieService;
import com.soundspace.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final CookieService cookieService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest registerRequest,
            HttpServletResponse response
    ) {
        String jwt = authenticationService.register(registerRequest);
        String email = registerRequest.getEmail();

        cookieService.setJwtAndRefreshCookie(jwt,
                refreshTokenService.createRefreshToken(email).getRefreshToken(), response);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(
            @RequestBody AuthenticationRequest authenticationRequest,
            HttpServletResponse response
    ) {
        String jwt = authenticationService.authenticate(authenticationRequest);
        String email = authenticationRequest.getEmail();

        cookieService.setJwtAndRefreshCookie(jwt,
                refreshTokenService.createRefreshToken(email).getRefreshToken(), response);

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
                refreshTokenService.createRefreshTokenAndRevokeOld(refreshToken);


        cookieService.setJwtAndRefreshCookie(refreshTokenCookieDto, response);
        return ResponseEntity.ok().build();
    }
}
