package com.bankapp.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private static final String ISSUER_URI = "http://localhost:8081/realms/spring-app";

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf().disable()
                .authorizeExchange()
                // Swagger / OpenAPI routes
                .pathMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-ui/index.html"
                ).permitAll()
                .anyExchange().authenticated()
                .and()
                .oauth2ResourceServer()
                .jwt();
        return http.build();
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return ReactiveJwtDecoders.fromIssuerLocation(ISSUER_URI);
    }
}