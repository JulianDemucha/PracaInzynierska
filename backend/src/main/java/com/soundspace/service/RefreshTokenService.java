package com.soundspace.service;

import com.soundspace.entity.AppUser;
import com.soundspace.entity.RefreshToken;
import com.soundspace.repository.AppUserRepository;
import com.soundspace.repository.RefreshTokenRepository;
import com.soundspace.dto.RefreshTokenCookieDto;
import com.soundspace.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final AppUserRepository appUserRepository;
    private final JwtService jwtService;

    public RefreshTokenCookieDto createRefreshToken(String email) {
        String token = UUID.randomUUID().toString();
        String hashedToken = DigestUtils.sha256Hex(token);
        Instant expiresAt = Instant.now().plusSeconds(60 * 60); //1h
        AppUser appUser = appUserRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User not found: " + email)
        );


        RefreshToken refreshToken = RefreshToken.builder()
                .appUser(appUser)
                .tokenHash(hashedToken)
                .expiresAt(expiresAt)
                .revoked(false)
                .revokedAt(null)
                .build();

        refreshTokenRepository.save(refreshToken);

        return RefreshTokenCookieDto.builder()
                .refreshToken(token)
                .expiresAt(expiresAt.toString())
                .jwt(jwtService.generateJwtToken(appUser))
                .build();
    }

    @Transactional
    public RefreshTokenCookieDto createNewRefreshToken(String currentToken) {
        String newToken = UUID.randomUUID().toString();
        String hashedNewToken = DigestUtils.sha256Hex(newToken);
        Instant expiresAt = Instant.now().plusSeconds(60 * 60); //1h
        AppUser appUser = getRefreshTokenByToken(currentToken).getAppUser();

        RefreshToken newRefreshToken = RefreshToken.builder()
                .appUser(appUser)
                .tokenHash(hashedNewToken)
                .expiresAt(expiresAt)
                .revoked(false)
                .revokedAt(null)
                .build();

        refreshTokenRepository.save(newRefreshToken);

        return RefreshTokenCookieDto.builder()
                .refreshToken(newToken)
                .expiresAt(expiresAt.toString())
                .jwt(jwtService.generateJwtToken(appUser))
                .build();
    }

    public void revokeRefreshToken(String refreshToken) {
        RefreshToken refreshTokenToRevoke = getRefreshTokenByToken(refreshToken);
        refreshTokenToRevoke.setRevoked(true);
        refreshTokenToRevoke.setRevokedAt(Instant.now());
        refreshTokenRepository.save(refreshTokenToRevoke);
    }

    public RefreshToken getRefreshTokenByToken(String token){
        return refreshTokenRepository.findByTokenHash(
                DigestUtils.sha256Hex(token)
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Refresh Token"));
    }

    @Transactional
    public int deleteRevokedRefreshTokens(int secondsBeforeCutoff) {
        return refreshTokenRepository.deleteRevokedBeforeCutoff(Instant.now().minusSeconds(secondsBeforeCutoff));
    }

}
