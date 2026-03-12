package com.bankapp.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthorizationHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String auth = headers.getFirst(HttpHeaders.AUTHORIZATION);

        if (auth != null) {
            exchange.getRequest().mutate()
                    .header(HttpHeaders.AUTHORIZATION, auth)
                    .build();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // s'exécute avant les autres filtres
    }
}