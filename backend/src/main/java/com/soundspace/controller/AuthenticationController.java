package com.soundspace.controller;

import com.soundspace.security.dto.AuthenticationRequest;
import com.soundspace.security.dto.RegisterRequest;
import com.soundspace.service.AuthenticationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest registerRequest,
            HttpServletResponse response
    ) {
        String jwt = authenticationService.register(registerRequest);
        ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(false) //http
                .path("/")
                .maxAge(60 * 60 * 24) // 24h
                .sameSite("Lax") //todo proxy on front (vite config)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(cookie);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(
            @RequestBody AuthenticationRequest authenticationRequest,
            HttpServletResponse response
    ) {
        String jwt = authenticationService.authenticate(authenticationRequest);
        ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(false) //http
                .path("/")
                .maxAge(60 * 60 * 24) // 24h
                .sameSite("Lax") //todo proxy on front (vite config)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(cookie);
    }
}
