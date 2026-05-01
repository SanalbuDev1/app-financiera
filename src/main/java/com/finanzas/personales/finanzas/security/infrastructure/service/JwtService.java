package com.finanzas.personales.finanzas.security.infrastructure.service;

import com.finanzas.personales.finanzas.auth.domain.model.User;
import com.finanzas.personales.finanzas.security.domain.port.TokenServicePort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Implementación del puerto {@link TokenServicePort} usando la librería JJWT.
 * Genera y valida tokens JWT con firma HMAC-SHA. El secret y la expiración
 * se leen desde {@code application.properties} para no tener valores hardcodeados.
 */
@Service
public class JwtService implements TokenServicePort {

    /** Secret para firmar el JWT, leído desde application.properties. */
    @Value("${app.jwt.secret}")
    private String secret;

    /** Tiempo de expiración del token en milisegundos (24h = 86400000). */
    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    /**
     * Genera un token JWT para el usuario autenticado o registrado.
     * El payload incluye: sub (email), id, name y role.
     *
     * @param user modelo de dominio del usuario
     * @return token JWT firmado como String
     */
    @Override
    public String generateToken(User user) {
        // Construir la clave HMAC-SHA a partir del secret configurado
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("id", user.getId())
                .claim("name", user.getName())
                .claim("role", user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Extrae los claims (payload) de un token JWT previamente firmado.
     *
     * @param token token JWT a parsear
     * @return {@link Claims} con todos los datos del payload
     * @throws io.jsonwebtoken.JwtException si el token es inválido o expiró
     */
    public Claims extractClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Verifica si un token JWT es válido (firma correcta y no expirado).
     *
     * @param token token JWT a validar
     * @return {@code true} si el token es válido, {@code false} si no
     */
    @Override
    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            // Token inválido, expirado o con firma incorrecta
            return false;
        }
    }

    /**
     * Extrae el email (subject) del token JWT.
     *
     * @param token token JWT
     * @return email del usuario
     */
    @Override
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Extrae el ID (UUID) del usuario del token JWT.
     *
     * @param token token JWT
     * @return id del usuario como String
     */
    @Override
    public String extractId(String token) {
        return extractClaims(token).get("id", String.class);
    }

    /**
     * Extrae el rol del usuario del token JWT.
     *
     * @param token token JWT
     * @return rol como String (ej. "ADMIN", "USER")
     */
    @Override
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }
}
