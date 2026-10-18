package com.bankapp.leave.config;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigJwtRolesTest {

    private final JwtAuthenticationConverter converter =
        new SecurityConfig().jwtAuthenticationConverter();

    private Jwt jwtWithRealmRoles(Object realmAccess) {
        Jwt.Builder builder = Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .subject("test-user")
            .issuedAt(Instant.parse("2026-01-01T00:00:00Z"))
            .expiresAt(Instant.parse("2027-01-01T00:00:00Z"));

        if (realmAccess != null) {
            builder.claim("realm_access", realmAccess);
        }

        return builder.build();
    }

    private Set<String> authorities(Jwt jwt) {
        return converter.convert(jwt).getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
    }

    @Test
    void convertsUserRealmRoleToSpringAuthority() {
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", Collections.singletonList("USER"));

        assertEquals(
            Set.of("ROLE_USER"),
            authorities(jwtWithRealmRoles(realmAccess))
        );
    }

    @Test
    void convertsAdminRealmRoleToSpringAuthority() {
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", Collections.singletonList("ADMIN"));

        assertEquals(
            Set.of("ROLE_ADMIN"),
            authorities(jwtWithRealmRoles(realmAccess))
        );
    }

    @Test
    void convertsMultipleRealmRoles() {
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", Arrays.asList("USER", "ADMIN"));

        assertEquals(
            Set.of("ROLE_USER", "ROLE_ADMIN"),
            authorities(jwtWithRealmRoles(realmAccess))
        );
    }

    @Test
    void missingRealmAccessProducesNoAuthorities() {
        assertTrue(authorities(jwtWithRealmRoles(null)).isEmpty());
    }

    @Test
    void missingRolesProducesNoAuthorities() {
        Map<String, Object> realmAccess = new HashMap<>();

        assertTrue(authorities(jwtWithRealmRoles(realmAccess)).isEmpty());
    }

    @Test
    void nonCollectionRolesProducesNoAuthorities() {
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", "ADMIN");

        assertTrue(authorities(jwtWithRealmRoles(realmAccess)).isEmpty());
    }
}
