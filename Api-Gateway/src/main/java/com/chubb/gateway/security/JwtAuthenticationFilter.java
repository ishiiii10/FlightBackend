package com.chubb.gateway.security;

import io.jsonwebtoken.Claims;
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

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            return chain.filter(exchange);
        }

        Claims claims = jwtService.extractAllClaims(token);

        String userId = claims.get("userId", String.class);
        String email = claims.getSubject();
        String role = claims.get("role", String.class);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(req -> req.headers(headers -> {
                    headers.set("X-User-Id", userId);
                    headers.set("X-User-Email", email);
                    headers.set("X-User-Role", role);
                }))
                .build();

        return chain.filter(mutatedExchange)
                .contextWrite(
                        ReactiveSecurityContextHolder.withAuthentication(authentication)
                );
    }
}