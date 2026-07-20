package com.example.itday.global.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final Duration accessTokenExpiration;
    private final Duration refreshTokenExpiration;

    public JwtUtil(
            @Value("${jwt.token.secretKey}") String secretKey,
            @Value("${jwt.token.expiration.access}") Duration accessTokenExpiration,
            @Value("${jwt.token.expiration.refresh}") Duration refreshTokenExpiration
    ){
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String createAccessToken(Long memberId) {
        return createToken(memberId, accessTokenExpiration);
    }

    public String createRefreshToken(Long memberId) {
        return createToken(memberId, refreshTokenExpiration);
    }

    private String createToken(Long memberId, Duration expiration) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + expiration.toMillis());

        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .setIssuedAt(now)
                .setExpiration(expiresAt)
                .signWith(secretKey)
                .compact();
    }

    public Long getMemberId(String token) {
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    public boolean validateToken(String token) {
        try{
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .setAllowedClockSkewSeconds(60)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
