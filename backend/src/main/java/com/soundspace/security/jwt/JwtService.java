package com.soundspace.security.jwt;

import com.soundspace.config.ApplicationConfigProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {
    private final ApplicationConfigProperties.JwtConfig jwtConfig;

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date(System.currentTimeMillis()));
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        //getUsername zwraca email (AppUser.java : 74)
        return (email.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername()) // getUsername() zwraca email
                .setIssuedAt(new Date(System.currentTimeMillis()))
                /// x1000, bo przy tworzeniu daty uzywajac currentTimeMills, data bedzie wyliczona w milisekundach
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.expirationSeconds()*1000 )) // 60 minut
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public String generateJwtToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }


    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtConfig.secret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
