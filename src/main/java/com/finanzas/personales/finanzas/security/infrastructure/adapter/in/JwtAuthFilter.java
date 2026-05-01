package com.finanzas.personales.finanzas.security.infrastructure.adapter.in;

import com.finanzas.personales.finanzas.security.domain.port.TokenServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Adaptador de entrada (input adapter) que intercepta cada petición HTTP para validar el token JWT.
 * Si el header {@code Authorization: Bearer <token>} es válido, establece el contexto
 * de seguridad de Spring para la solicitud actual. Implementa {@link WebFilter} de WebFlux.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements WebFilter {

    /** Puerto del dominio para operaciones de token. */
    private final TokenServicePort tokenServicePort;

    /**
     * Intercepta cada solicitud HTTP, extrae el JWT del header Authorization
     * y, si es válido, establece la autenticación en el contexto reactivo de seguridad.
     *
     * @param exchange el intercambio HTTP actual (request + response)
     * @param chain    la cadena de filtros a continuar
     * @return {@code Mono<Void>} que propaga la solicitud al siguiente filtro
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // Verificar que el header existe y tiene el formato "Bearer <token>"
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);

        // Validar el token; si es inválido, continuar sin autenticación
        if (!tokenServicePort.isTokenValid(token)) {
            return chain.filter(exchange);
        }

        // Extraer userId, email y rol del token para construir la autenticación
        String userId = tokenServicePort.extractId(token);
        String email = tokenServicePort.extractEmail(token);
        String role = tokenServicePort.extractRole(token);

        // Construir el objeto de autenticación con userId como principal y email como credentials
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId,
                email,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );

        // Establecer la autenticación en el contexto reactivo de seguridad
        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }
}
