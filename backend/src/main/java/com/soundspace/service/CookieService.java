package com.soundspace.service;

import com.soundspace.dto.RefreshTokenCookieDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CookieService {

    private final RefreshTokenService refreshTokenService;

    public ResponseCookie createJwtCookie(String jwt, int maxAgeSeconds) {
        return ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(false) //http
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax")  //todo proxy on front (vite config)
                .build();
    }

    public ResponseCookie createRefreshCookie(String refreshToken, int maxAgeSeconds) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) //http
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Strict")  //todo proxy on front (vite config)
                .build();
    }

    public void setJwtAndRefreshCookie(String jwt, String refreshToken, HttpServletResponse response, int jwtMaxAgeSeconds, int refreshTokenMaxAgeSeconds) {

        response.addHeader(HttpHeaders.SET_COOKIE,
                createJwtCookie(jwt, jwtMaxAgeSeconds)
                        .toString());

        response.addHeader(HttpHeaders.SET_COOKIE,
                createRefreshCookie(refreshToken, refreshTokenMaxAgeSeconds)
                        .toString());



    }

    // UZYWAC TYLKO W RAZIE REFRESHU REFRESHCOOKIE, NIE TWORZENIU NOWEGO, INACZEJ JWT == NULL
    public void setJwtAndRefreshCookie(RefreshTokenCookieDto refreshTokenCookieDto, HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE,
               createJwtCookie(refreshTokenCookieDto.getJwt(),
                                60 * 60 * 24 /* 1h */)
                        .toString());
        response.addHeader(HttpHeaders.SET_COOKIE, createRefreshCookie(refreshTokenCookieDto.getRefreshToken(),
                        60 * 60 * 24 * 30 /* 24h */)
                .toString());
    }


}
