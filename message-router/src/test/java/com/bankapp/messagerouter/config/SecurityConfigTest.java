package com.bankapp.messagerouter.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigTest {

    private final JwtAuthenticationConverter converter =
            new SecurityConfig().jwtAuthenticationConverter();

    private Jwt createJwt(Map<String, Object> claims) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claims(existing -> existing.putAll(claims))
                .build();
    }

    @Test
    void jwtConverter_shouldPrefixKeycloakRolesWithRole() {
        Jwt jwt = createJwt(Map.of(
                "realm_access", Map.of("roles", List.of("ADMIN", "USER"))
        ));

        Set<String> authorities = converter.convert(jwt)
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertEquals(Set.of("ROLE_ADMIN", "ROLE_USER"), authorities);
    }

    @Test
    void jwtConverter_shouldReturnNoAuthoritiesWhenRealmAccessIsMissing() {
        Jwt jwt = createJwt(Map.of("preferred_username", "alice"));

        assertTrue(converter.convert(jwt).getAuthorities().isEmpty());
    }

    @Test
    void jwtConverter_shouldReturnNoAuthoritiesWhenRolesAreMissing() {
        Jwt jwt = createJwt(Map.of("realm_access", Map.of()));

        assertTrue(converter.convert(jwt).getAuthorities().isEmpty());
    }

    @Test
    void jwtConverter_shouldReturnNoAuthoritiesWhenRolesAreNotACollection() {
        Jwt jwt = createJwt(Map.of(
                "realm_access", Map.of("roles", "ADMIN")
        ));

        assertTrue(converter.convert(jwt).getAuthorities().isEmpty());
    }

    @Test
    void jwtConverter_shouldIgnoreNonStringRoleEntries() {
        Jwt jwt = createJwt(Map.of(
                "realm_access",
                Map.of("roles", List.of("ADMIN", 123))
        ));

        Set<String> authorities = converter.convert(jwt)
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        assertEquals(Set.of("ROLE_ADMIN"), authorities);
    }
}
