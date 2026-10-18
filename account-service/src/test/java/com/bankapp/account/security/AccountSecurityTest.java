package com.bankapp.account.security;

import com.bankapp.account.config.SecurityConfig;
import com.bankapp.account.domain.AccountDto;
import com.bankapp.account.controller.AccountController;
import com.bankapp.account.service.AccountService;
import com.bankapp.account.service.AccountTransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class)
class AccountSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SecurityConfig securityConfig;

    @MockBean
    private AccountService accountService;

    @MockBean
    private AccountTransferService accountTransferService;

    @Test
    void anonymousRequestToProtectedApi_returns401() throws Exception {
        mockMvc.perform(get("/api/accounts/my"))
                .andExpect(status().isUnauthorized());

        verify(accountService, never()).getAccountsByOwner(any());
    }

    @Test
    void authenticatedUserWithoutRequiredRole_cannotTransfer() throws Exception {
        String body = "{"
                + "\"sourceAccountId\":1,"
                + "\"destinationAccountId\":2,"
                + "\"amount\":25.00,"
                + "\"currency\":\"TND\","
                + "\"reference\":\"REF_123\""
                + "}";

        mockMvc.perform(post("/api/accounts/transfers")
                        .with(jwt().authorities(Collections.emptyList()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        verify(accountTransferService, never()).transfer(any(), any(), any());
    }

    @Test
    void keycloakRealmRoles_areConvertedToSpringAuthorities() {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("user-123")
                .issuedAt(Instant.parse("2026-01-01T00:00:00Z"))
                .expiresAt(Instant.parse("2027-01-01T00:00:00Z"))
                .claim("realm_access", Map.of(
                        "roles", Arrays.asList("USER", "ADMIN", 123)))
                .build();

        org.springframework.security.authentication.AbstractAuthenticationToken authentication =
                securityConfig.jwtAuthenticationConverter().convert(jwt);

        org.junit.jupiter.api.Assertions.assertNotNull(authentication);
        org.junit.jupiter.api.Assertions.assertTrue(
                authentication.getAuthorities().contains(
                        new SimpleGrantedAuthority("ROLE_USER")));
        org.junit.jupiter.api.Assertions.assertTrue(
                authentication.getAuthorities().contains(
                        new SimpleGrantedAuthority("ROLE_ADMIN")));
        org.junit.jupiter.api.Assertions.assertEquals(
                2, authentication.getAuthorities().size());
    }


    @Test
    void userCannotCreateAccountForAnotherOwner() throws Exception {
        String body = "{\"ownerId\":\"other-user\",\"currency\":\"TND\"}";

        mockMvc.perform(post("/api/accounts")
                        .with(jwt()
                                .jwt(token -> token
                                        .subject("user-123")
                                        .claim("preferred_username", "youssef"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        verify(accountService, never()).createAccount(any());
    }

    @Test
    void userCannotReadAnotherUsersAccountById() throws Exception {
        AccountDto otherAccount = AccountDto.builder()
                .id(99L)
                .ownerId("other-user")
                .accountNumber("TN1234567890123456789012")
                .currency("TND")
                .balance(new BigDecimal("100.00"))
                .status("ACTIVE")
                .createdAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();

        org.mockito.Mockito.when(accountService.getAccountById(99L))
                .thenReturn(otherAccount);

        mockMvc.perform(get("/api/accounts/99")
                        .with(jwt()
                                .jwt(token -> token
                                        .subject("user-123")
                                        .claim("preferred_username", "youssef"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCannotListAnotherOwnersAccounts() throws Exception {
        mockMvc.perform(get("/api/accounts/owner/other-user")
                        .with(jwt()
                                .jwt(token -> token
                                        .subject("user-123")
                                        .claim("preferred_username", "youssef"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());

        verify(accountService, never()).getAccountsByOwner("other-user");
    }

    @Test
    void adminCanReadAnotherUsersAccount() throws Exception {
        AccountDto otherAccount = AccountDto.builder()
                .id(99L)
                .ownerId("other-user")
                .accountNumber("TN1234567890123456789012")
                .currency("TND")
                .balance(new BigDecimal("100.00"))
                .status("ACTIVE")
                .createdAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();

        org.mockito.Mockito.when(accountService.getAccountById(99L))
                .thenReturn(otherAccount);

        mockMvc.perform(get("/api/accounts/99")
                        .with(jwt()
                                .jwt(token -> token
                                        .subject("admin-123")
                                        .claim("preferred_username", "admin"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.ownerId").value("other-user"));
    }

}
