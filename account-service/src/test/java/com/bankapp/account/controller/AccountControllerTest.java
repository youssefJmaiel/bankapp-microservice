package com.bankapp.account.controller;

import com.bankapp.account.domain.AccountDto;
import com.bankapp.account.domain.AccountTransferRequest;
import com.bankapp.account.domain.CreateAccountRequest;
import com.bankapp.account.entity.AccountTransfer;
import com.bankapp.account.service.AccountService;
import com.bankapp.account.service.AccountTransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import(AccountControllerTestSecurityConfig.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @MockBean
    private AccountTransferService accountTransferService;

    private AccountDto account(Long id, String owner, String number) {
        return AccountDto.builder()
                .id(id)
                .ownerId(owner)
                .accountNumber(number)
                .currency("TND")
                .balance(new BigDecimal("100.00"))
                .status("ACTIVE")
                .createdAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
    }

    private Jwt jwtFor(String subject, String username) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(subject)
                .claim("preferred_username", username)
                .issuedAt(Instant.parse("2026-01-01T00:00:00Z"))
                .expiresAt(Instant.parse("2027-01-01T00:00:00Z"))
                .build();
    }

    @Test
    void createAccount_validRequest_returnsCreatedAccount() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setOwnerId("user-123");
        request.setCurrency("TND");

        when(accountService.createAccount(any(CreateAccountRequest.class)))
                .thenReturn(account(1L, "user-123", "TN1234567890123456789012"));

        mockMvc.perform(post("/api/accounts")
                        .with(jwt().jwt(jwtFor("user-123", "youssef")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ownerId").value("user-123"))
                .andExpect(jsonPath("$.currency").value("TND"));

        verify(accountService).createAccount(any(CreateAccountRequest.class));
    }

    @Test
    void createAccount_invalidOwner_returnsBadRequestWithoutCallingService()
            throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .with(jwt().jwt(jwtFor("user-123", "youssef")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":\"\",\"currency\":\"TND\"}"))
                .andExpect(status().isBadRequest());

        verify(accountService, never()).createAccount(any(CreateAccountRequest.class));
    }

    @Test
    void createAccount_invalidCurrency_returnsBadRequestWithoutCallingService()
            throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .with(jwt().jwt(jwtFor("user-123", "youssef")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":\"user-123\",\"currency\":\"tn1\"}"))
                .andExpect(status().isBadRequest());

        verify(accountService, never()).createAccount(any(CreateAccountRequest.class));
    }

    @Test
    void getMyAccounts_returnsAccountsForJwtSubjectAndUsername() throws Exception {
        when(accountService.getAccountsByOwner("user-123"))
                .thenReturn(new java.util.ArrayList<>(
                        Collections.singletonList(
                                account(1L, "user-123", "TN1234567890123456789012"))));
        when(accountService.getAccountsByOwner("youssef"))
                .thenReturn(Collections.singletonList(
                        account(2L, "youssef", "TN2234567890123456789012")));

        mockMvc.perform(get("/api/accounts/my")
                        .with(jwt().jwt(jwtFor("user-123", "youssef"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].ownerId").value("user-123"))
                .andExpect(jsonPath("$[1].ownerId").value("youssef"));

        verify(accountService).getAccountsByOwner("user-123");
        verify(accountService).getAccountsByOwner("youssef");
    }

    @Test
    void getMyAccounts_doesNotQueryUsernameTwiceWhenSameAsSubject()
            throws Exception {
        when(accountService.getAccountsByOwner("youssef"))
                .thenReturn(new java.util.ArrayList<>());

        mockMvc.perform(get("/api/accounts/my")
                        .with(jwt().jwt(jwtFor("youssef", "youssef"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(accountService).getAccountsByOwner("youssef");
    }

    @Test
    void getAccountById_returnsAccount() throws Exception {
        when(accountService.getAccountById(1L))
                .thenReturn(account(1L, "user-123", "TN1234567890123456789012"));

        mockMvc.perform(get("/api/accounts/1")
                        .with(jwt().jwt(jwtFor("user-123", "youssef"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountNumber")
                        .value("TN1234567890123456789012"));
    }

    @Test
    void getAccountByNumber_returnsAccount() throws Exception {
        when(accountService.getAccountByNumber("TN1234567890123456789012"))
                .thenReturn(account(1L, "user-123", "TN1234567890123456789012"));

        mockMvc.perform(get("/api/accounts/number/TN1234567890123456789012")
                        .with(jwt().jwt(jwtFor("user-123", "youssef"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerId").value("user-123"));
    }

    @Test
    void getAccountsByOwner_returnsAccounts() throws Exception {
        when(accountService.getAccountsByOwner("user-123"))
                .thenReturn(Collections.singletonList(
                        account(1L, "user-123", "TN1234567890123456789012")));

        mockMvc.perform(get("/api/accounts/owner/user-123")
                        .with(jwt().jwt(jwtFor("user-123", "youssef"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void transfer_validRequestPassesJwtIdentityToService() throws Exception {
        AccountTransfer transfer = new AccountTransfer();
        transfer.setReference("REF_123");
        transfer.setSourceAccountId(1L);
        transfer.setDestinationAccountId(2L);
        transfer.setAmount(new BigDecimal("25.00"));
        transfer.setCurrency("TND");

        when(accountTransferService.transfer(
                any(AccountTransferRequest.class), eq("user-123"), eq("youssef")))
                .thenReturn(transfer);

        String body = "{"
                + "\"sourceAccountId\":1,"
                + "\"destinationAccountId\":2,"
                + "\"amount\":25.00,"
                + "\"currency\":\"TND\","
                + "\"reference\":\"REF_123\""
                + "}";

        mockMvc.perform(post("/api/accounts/transfers")
                        .with(jwt().jwt(jwtFor("user-123", "youssef"))
                                .authorities(() -> "ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reference").value("REF_123"))
                .andExpect(jsonPath("$.sourceAccountId").value(1))
                .andExpect(jsonPath("$.destinationAccountId").value(2));

        verify(accountTransferService).transfer(
                any(AccountTransferRequest.class), eq("user-123"), eq("youssef"));
    }

    @Test
    void transfer_missingDestination_returnsBadRequestWithoutCallingService()
            throws Exception {
        String body = "{"
                + "\"sourceAccountId\":1,"
                + "\"amount\":25.00,"
                + "\"currency\":\"TND\","
                + "\"reference\":\"REF_123\""
                + "}";

        mockMvc.perform(post("/api/accounts/transfers")
                        .with(jwt().jwt(jwtFor("user-123", "youssef"))
                                .authorities(() -> "ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(accountTransferService, never()).transfer(
                any(AccountTransferRequest.class), any(), any());
    }

    @Test
    void transfer_invalidAmount_returnsBadRequestWithoutCallingService()
            throws Exception {
        String body = "{"
                + "\"sourceAccountId\":1,"
                + "\"destinationAccountId\":2,"
                + "\"amount\":0,"
                + "\"currency\":\"TND\","
                + "\"reference\":\"REF_123\""
                + "}";

        mockMvc.perform(post("/api/accounts/transfers")
                        .with(jwt().jwt(jwtFor("user-123", "youssef"))
                                .authorities(() -> "ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(accountTransferService, never()).transfer(
                any(AccountTransferRequest.class), any(), any());
    }

    @Test
    void getMyTransfers_withoutAccounts_returnsEmptyList() throws Exception {
        when(accountService.getAccountsByOwner("user-123"))
                .thenReturn(new java.util.ArrayList<>());

        mockMvc.perform(get("/api/accounts/my/transfers")
                        .with(jwt().jwt(jwtFor("user-123", "user-123"))
                                .authorities(() -> "ROLE_USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(accountTransferService, never()).findTransfersForAccounts(any());
    }
}
