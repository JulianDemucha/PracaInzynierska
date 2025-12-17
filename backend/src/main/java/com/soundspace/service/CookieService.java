package com.soundspace.service;

import com.soundspace.config.ApplicationConfigProperties;
import com.soundspace.dto.RefreshTokenCookieDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CookieService {
    private final ApplicationConfigProperties.JwtConfig jwtConfig;
    private final ApplicationConfigProperties.CookieConfig cookieConfig;

    public ResponseCookie createJwtCookie(String jwt) {
        return createCookie("jwt", jwt, jwtConfig.expirationSeconds());
    }

    public ResponseCookie createRefreshCookie(String refreshToken) {
        return createCookie(refreshToken, refreshToken, jwtConfig.refreshExpirationSeconds());
    }

    public void setJwtAndRefreshCookie(String jwt, String refreshToken, HttpServletResponse response) {

        response.addHeader(HttpHeaders.SET_COOKIE,
                createJwtCookie(jwt).toString());

        response.addHeader(HttpHeaders.SET_COOKIE,
                createRefreshCookie(refreshToken).toString());

    }

    // UZYWAC TYLKO W RAZIE REFRESHU REFRESHCOOKIE, NIE TWORZENIU NOWEGO, INACZEJ JWT == NULL
    public void setJwtAndRefreshCookie(RefreshTokenCookieDto refreshTokenCookieDto, HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                createJwtCookie(refreshTokenCookieDto.getJwt())
                        .toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                createRefreshCookie(refreshTokenCookieDto.getRefreshToken())
                        .toString());
    }


    // helpery

    private ResponseCookie createCookie(String name, String value, long maxAgeSeconds) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(cookieConfig.httpOnly())
                .secure(cookieConfig.secure())
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite(cookieConfig.sameSite());

        // zostawiam w razie mitycznego prod
//        if (cookieConfig.domain() != null && !cookieConfig.domain().isBlank()) {
//            builder.domain(cookieConfig.domain());
//        }

        return builder.build();
    }


}
