package com.finanzas.personales.finanzas.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
 * Filtro reactivo de seguridad que intercepta cada petición HTTP para validar el token JWT.
 * Si el header {@code Authorization: Bearer <token>} es válido, establece el contexto
 * de seguridad de Spring para la solicitud actual. Implementa {@link WebFilter} de WebFlux.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements WebFilter {

    /** Servicio encargado de validar y extraer datos del JWT. */
    private final JwtService jwtService;

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
        if (!jwtService.isTokenValid(token)) {
            return chain.filter(exchange);
        }

        // Extraer email y rol del token para construir la autenticación
        String email = jwtService.extractEmail(token);
        String role = jwtService.extractRole(token);

        // Construir el objeto de autenticación con el rol como authority
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );

        // Establecer la autenticación en el contexto reactivo de seguridad
        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }
}
