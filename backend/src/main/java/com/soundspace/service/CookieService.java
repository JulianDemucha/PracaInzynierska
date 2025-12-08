package com.soundspace.service;

import com.soundspace.config.ApplicationConfigProperties;
import com.soundspace.dto.RefreshTokenCookieDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

//todo zrobic CookieConfig, gdzie w application.yaml bedzie pobierac dane z .env (prod mialoby secure na true itp.)
@Service
@RequiredArgsConstructor
public class CookieService {
    private final ApplicationConfigProperties.JwtConfig jwtConfig;

    public ResponseCookie createJwtCookie(String jwt) {
        return ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(false) //http
                .path("/")
                .maxAge(jwtConfig.expirationSeconds())
                .sameSite("Lax")  //todo proxy on front (vite config)
                .build();
    }

    public ResponseCookie createRefreshCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) //http
                .path("/")
                .maxAge(jwtConfig.refreshExpirationSeconds())
                .sameSite("Strict")  //todo proxy on front (vite config)
                .build();
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


}
