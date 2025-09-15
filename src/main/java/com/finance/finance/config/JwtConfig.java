package com.finance.finance.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtConfig {

    @Value("${jwt.secret:FinanceAppSecretKey2024!@#$%^&*()FinanceAppSecretKey2024!@#$%^&*()FinanceAppSecretKey2024!@#$%^&*()FinanceAppSecretKey2024!@#$%^&*()}")
    private String secret;

    @Value("${jwt.expiration:3600000}") // 1 hora em milissegundos
    private Long expiration;

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    public String generateToken(String username, String roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private SecretKey getSigningKey() {
        // Use the configured secret from application.properties
        // Convert the string secret to a proper SecretKey for HS512
        byte[] keyBytes = secret.getBytes();
        // Ensure the key is exactly 512 bits (64 bytes) as required by HS512
        if (keyBytes.length != 64) {
            // If the secret is not exactly 64 bytes, pad or truncate it
            byte[] adjustedKey = new byte[64];
            System.arraycopy(keyBytes, 0, adjustedKey, 0, Math.min(keyBytes.length, 64));
            return Keys.hmacShaKeyFor(adjustedKey);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Long getExpiration() {
        return expiration;
    }
}
