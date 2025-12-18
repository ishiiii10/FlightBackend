package com.chubb.gateway.config;

import com.chubb.gateway.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        // 🔓 Public endpoints (GENERAL PUBLIC)
                        .pathMatchers("/auth/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/flights/**").permitAll()

                        // 👤 Customer endpoints
                        .pathMatchers("/bookings/**").hasRole("CUSTOMER")

                        // 🛠 Admin-only flight management
                        .pathMatchers(HttpMethod.POST, "/flights/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/flights/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/flights/**").hasRole("ADMIN")

                        // Everything else must be authenticated
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .cors(cors -> cors.configurationSource(exchange -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:4200"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .build();
    }
}