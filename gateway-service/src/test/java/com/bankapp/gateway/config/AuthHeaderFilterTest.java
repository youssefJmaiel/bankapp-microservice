package com.bankapp.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AuthHeaderFilterTest {

    private final AuthHeaderFilter filter = new AuthHeaderFilter();

    @Test
    void shouldForwardAuthorizationHeaderToDownstreamExchange() {
        String token = "Bearer test-token";
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/accounts")
                        .header(HttpHeaders.AUTHORIZATION, token)
        );
        AtomicReference<ServerWebExchange> forwardedExchange =
                new AtomicReference<>();

        GatewayFilterChain chain = nextExchange -> {
            forwardedExchange.set(nextExchange);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertNotNull(forwardedExchange.get());
        assertEquals(
                token,
                forwardedExchange.get()
                        .getRequest()
                        .getHeaders()
                        .getFirst(HttpHeaders.AUTHORIZATION)
        );
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderIsAbsent() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/accounts")
        );
        AtomicReference<ServerWebExchange> forwardedExchange =
                new AtomicReference<>();

        GatewayFilterChain chain = nextExchange -> {
            forwardedExchange.set(nextExchange);
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertSame(exchange, forwardedExchange.get());
        assertNull(
                forwardedExchange.get()
                        .getRequest()
                        .getHeaders()
                        .getFirst(HttpHeaders.AUTHORIZATION)
        );
    }

    @Test
    void shouldHaveOrderMinusOne() {
        assertEquals(-1, filter.getOrder());
    }
}
