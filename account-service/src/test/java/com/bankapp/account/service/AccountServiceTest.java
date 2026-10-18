package com.bankapp.account.service;

import com.bankapp.account.entity.Account;
import com.bankapp.account.domain.AccountDto;
import com.bankapp.account.domain.CreateAccountRequest;
import com.bankapp.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private CreateAccountRequest request;

    @BeforeEach
    void setUp() {
        request = new CreateAccountRequest();
        request.setOwnerId("user-123");
        request.setCurrency("TND");
    }

    @Test
    void createAccountShouldUseTndWhenCurrencyIsNull() {
        request.setCurrency(null);

        when(accountRepository.existsByAccountNumber(anyString()))
                .thenReturn(false);
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountDto result = accountService.createAccount(request);

        assertNotNull(result);
        assertEquals("user-123", result.getOwnerId());
        assertEquals("TND", result.getCurrency());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertEquals("ACTIVE", result.getStatus());
        assertTrue(result.getAccountNumber().matches("TN[0-9]{22}"));

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccountShouldPreserveSpecifiedCurrency() {
        request.setCurrency("EUR");

        when(accountRepository.existsByAccountNumber(anyString()))
                .thenReturn(false);
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountDto result = accountService.createAccount(request);

        assertEquals("EUR", result.getCurrency());
        assertEquals("user-123", result.getOwnerId());
    }

    @Test
    void createAccountShouldRetryWhenAccountNumberAlreadyExists() {
        when(accountRepository.existsByAccountNumber(anyString()))
                .thenReturn(true, false);
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountDto result = accountService.createAccount(request);

        assertTrue(result.getAccountNumber().matches("TN[0-9]{22}"));
        verify(accountRepository, times(2))
                .existsByAccountNumber(anyString());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void getAccountByIdShouldReturnAccountWhenItExists() {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("TN1234567890123456789012");
        account.setOwnerId("user-123");
        account.setCurrency("TND");
        account.setBalance(new BigDecimal("250.00"));
        account.setStatus("ACTIVE");

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        AccountDto result = accountService.getAccountById(1L);

        assertEquals(1L, result.getId());
        assertEquals(account.getAccountNumber(), result.getAccountNumber());
        assertEquals("user-123", result.getOwnerId());
        assertEquals(new BigDecimal("250.00"), result.getBalance());
    }

    @Test
    void getAccountByIdShouldThrowWhenAccountDoesNotExist() {
        when(accountRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.getAccountById(99L)
        );
    }

    @Test
    void getAccountByNumberShouldReturnAccountWhenItExists() {
        Account account = new Account();
        account.setId(2L);
        account.setAccountNumber("TN1234567890123456789012");
        account.setOwnerId("user-456");
        account.setCurrency("EUR");
        account.setBalance(new BigDecimal("100.00"));
        account.setStatus("ACTIVE");

        when(accountRepository.findByAccountNumber(
                account.getAccountNumber()))
                .thenReturn(Optional.of(account));

        AccountDto result = accountService.getAccountByNumber(
                account.getAccountNumber());

        assertEquals(2L, result.getId());
        assertEquals("EUR", result.getCurrency());
        assertEquals(account.getAccountNumber(), result.getAccountNumber());
    }

    @Test
    void getAccountByNumberShouldThrowWhenAccountDoesNotExist() {
        String accountNumber = "TN1234567890123456789012";

        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.getAccountByNumber(accountNumber)
        );
    }

    @Test
    void getAccountsByOwnerShouldReturnAccountsBelongingToOwner() {
        Account first = new Account();
        first.setId(1L);
        first.setAccountNumber("TN1234567890123456789012");
        first.setOwnerId("user-123");
        first.setCurrency("TND");
        first.setBalance(new BigDecimal("100.00"));
        first.setStatus("ACTIVE");

        Account second = new Account();
        second.setId(2L);
        second.setAccountNumber("TN2234567890123456789012");
        second.setOwnerId("user-123");
        second.setCurrency("EUR");
        second.setBalance(new BigDecimal("200.00"));
        second.setStatus("ACTIVE");

        when(accountRepository.findByOwnerId("user-123"))
                .thenReturn(List.of(first, second));

        List<AccountDto> results =
                accountService.getAccountsByOwner("user-123");

        assertEquals(2, results.size());
        assertEquals("user-123", results.get(0).getOwnerId());
        assertEquals("user-123", results.get(1).getOwnerId());
        assertEquals("TND", results.get(0).getCurrency());
        assertEquals("EUR", results.get(1).getCurrency());

        verify(accountRepository).findByOwnerId("user-123");
    }

    @Test
    void getAccountsByOwnerShouldReturnEmptyListWhenOwnerHasNoAccounts() {
        when(accountRepository.findByOwnerId("unknown-user"))
                .thenReturn(List.of());

        List<AccountDto> results =
                accountService.getAccountsByOwner("unknown-user");

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}
