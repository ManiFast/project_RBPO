package ru.mtuci.coursemanagement.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.mtuci.coursemanagement.model.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTtlMinutes;
    private final long refreshTtlDays;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-ttl-minutes}") long accessTtlMinutes,
            @Value("${jwt.refresh-ttl-days}") long refreshTtlDays
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlMinutes = accessTtlMinutes;
        this.refreshTtlDays = refreshTtlDays;
    }

    public String generateAccessToken(User user, Long sessionId, String accessJti) {
        Instant now = Instant.now();
        Instant exp = now.plus(accessTtlMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("type", "access")
                .claim("userId", user.getId())
                .claim("role", user.getRole())
                .claim("sid", sessionId)
                .id(accessJti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(User user, Long sessionId, String refreshJti) {
        Instant now = Instant.now();
        Instant exp = now.plus(refreshTtlDays, ChronoUnit.DAYS);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim("type", "refresh")
                .claim("userId", user.getId())
                .claim("role", user.getRole())
                .claim("sid", sessionId)
                .id(refreshJti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(secretKey)
                .compact();
    }

    public boolean isValidAccessToken(String token) {
        return isValidTokenOfType(token, "access");
    }

    public boolean isValidRefreshToken(String token) {
        return isValidTokenOfType(token, "refresh");
    }

    private boolean isValidTokenOfType(String token, String expectedType) {
        try {
            Claims claims = getClaims(token);
            return expectedType.equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public Long getSessionId(String token) {
        Object sid = getClaims(token).get("sid");
        if (sid instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(sid));
    }

    public String getTokenId(String token) {
        return getClaims(token).getId();
    }

    public long getAccessTtlSeconds() {
        return accessTtlMinutes * 60L;
    }


    public long getRefreshTtlSeconds() {
        return refreshTtlDays * 24L * 60L * 60L;
    }
}



//срок жизни
