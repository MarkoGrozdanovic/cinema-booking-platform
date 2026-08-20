package com.cinemabooking.platform.security;

import com.cinemabooking.platform.model.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.util.Base64;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final String jwtSecret;
    private final long jwtExpirationMilliseconds;

    public JwtService(
            @Value("${security.jwt.secret}") String jwtSecret,
            @Value("${security.jwt.expiration-milliseconds}")
            long jwtExpirationMilliseconds
    ) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMilliseconds = jwtExpirationMilliseconds;
    }

    public String generateToken(AppUser user){
        Date issuedAt = new Date();
        Date expiration = new Date(
                issuedAt.getTime() + jwtExpirationMilliseconds
        );

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, AppUser user) {
        try {
            Claims claims = extractClaims(token);

            return claims.getSubject().equals(user.getEmail())
                    && claims.getExpiration().after(new Date())
                    && user.isActive();
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder()
                .decode(jwtSecret.trim());

        return Keys.hmacShaKeyFor(keyBytes);
    }

}
