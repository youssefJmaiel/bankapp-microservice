package com.bankapp.gateway.config;

import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

@Component
public class AuthHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null) {
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .build();
            return chain.filter(exchange.mutate().request(request).build());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // avant la plupart des filtres
    }
}