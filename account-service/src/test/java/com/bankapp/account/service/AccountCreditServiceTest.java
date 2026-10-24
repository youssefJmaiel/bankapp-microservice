package com.bankapp.account.service;

import com.bankapp.account.domain.AccountCreditDto;
import com.bankapp.account.domain.CreditAccountRequest;
import com.bankapp.account.entity.Account;
import com.bankapp.account.entity.AccountCredit;
import com.bankapp.account.repository.AccountCreditRepository;
import com.bankapp.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountCreditServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountCreditRepository creditRepository;

    @InjectMocks
    private AccountCreditService creditService;

    private CreditAccountRequest request;

    @BeforeEach
    void setUp() {
        request = new CreditAccountRequest();
        request.setAmount(new BigDecimal("50.00"));
        request.setReference("CREDIT_123");
    }

    private Account account(Long id, String balance, String currency, String status) {
        Account account = new Account();
        account.setId(id);
        account.setOwnerId("user-123");
        account.setBalance(new BigDecimal(balance));
        account.setCurrency(currency);
        account.setStatus(status);
        return account;
    }

    private AccountCredit existingCredit(
            Long accountId, String amount, String currency,
            String reference, String balanceAfter) {
        AccountCredit credit = new AccountCredit();
        credit.setId(10L);
        credit.setAccountId(accountId);
        credit.setAmount(new BigDecimal(amount));
        credit.setCurrency(currency);
        credit.setReference(reference);
        credit.setBalanceAfter(new BigDecimal(balanceAfter));
        return credit;
    }

    @Test
    void creditShouldIncreaseBalanceAndSaveHistory() {
        Account account = account(1L, "100.00", "TND", "ACTIVE");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        when(creditRepository.findByReference("CREDIT_123")).thenReturn(Optional.empty());
        when(creditRepository.saveAndFlush(any(AccountCredit.class)))
                .thenAnswer(invocation -> {
                    AccountCredit saved = invocation.getArgument(0);
                    saved.setId(10L);
                    return saved;
                });

        AccountCreditDto result = creditService.credit(1L, request);

        assertEquals(new BigDecimal("150.00"), account.getBalance());
        assertEquals(1L, result.getAccountId());
        assertEquals(new BigDecimal("50.00"), result.getAmount());
        assertEquals("TND", result.getCurrency());
        assertEquals("CREDIT_123", result.getReference());
        assertEquals(new BigDecimal("150.00"), result.getBalanceAfter());

        verify(accountRepository).findByIdForUpdate(1L);
        verify(accountRepository).save(account);
        verify(creditRepository).saveAndFlush(any(AccountCredit.class));
    }

    @Test
    void creditShouldReturnExistingCreditForSameReferenceWithoutCreditingAgain() {
        Account account = account(1L, "150.00", "TND", "ACTIVE");
        AccountCredit previous = existingCredit(
                1L, "50.00", "TND", "CREDIT_123", "150.00");

        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        when(creditRepository.findByReference("CREDIT_123")).thenReturn(Optional.of(previous));

        AccountCreditDto result = creditService.credit(1L, request);

        assertEquals(1L, result.getAccountId());
        assertEquals(new BigDecimal("150.00"), result.getBalanceAfter());
        assertEquals(new BigDecimal("150.00"), account.getBalance());
        verify(accountRepository, never()).save(any(Account.class));
        verify(creditRepository, never()).saveAndFlush(any(AccountCredit.class));
    }

    @Test
    void creditShouldRejectReferenceReusedWithDifferentAmount() {
        Account account = account(1L, "150.00", "TND", "ACTIVE");
        AccountCredit previous = existingCredit(
                1L, "25.00", "TND", "CREDIT_123", "125.00");

        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        when(creditRepository.findByReference("CREDIT_123")).thenReturn(Optional.of(previous));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> creditService.credit(1L, request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(accountRepository, never()).save(any(Account.class));
        verify(creditRepository, never()).saveAndFlush(any(AccountCredit.class));
    }

    @Test
    void creditShouldReturnNotFoundWhenAccountDoesNotExist() {
        when(accountRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> creditService.credit(99L, request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(creditRepository, never()).saveAndFlush(any(AccountCredit.class));
    }

    @Test
    void creditShouldRejectInactiveAccount() {
        Account account = account(1L, "100.00", "TND", "BLOCKED");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        when(creditRepository.findByReference("CREDIT_123")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> creditService.credit(1L, request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals(new BigDecimal("100.00"), account.getBalance());
        verify(accountRepository, never()).save(any(Account.class));
        verify(creditRepository, never()).saveAndFlush(any(AccountCredit.class));
    }

    @Test
    void creditShouldRejectZeroAmountBeforeAccessingRepositories() {
        request.setAmount(BigDecimal.ZERO);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> creditService.credit(1L, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verifyNoInteractions(accountRepository, creditRepository);
    }

    @Test
    void creditShouldRejectAmountWithMoreThanTwoDecimalPlaces() {
        request.setAmount(new BigDecimal("10.001"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> creditService.credit(1L, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verifyNoInteractions(accountRepository, creditRepository);
    }

    @Test
    void creditShouldRejectInvalidReferenceBeforeAccessingRepositories() {
        request.setReference("bad reference!");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> creditService.credit(1L, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verifyNoInteractions(accountRepository, creditRepository);
    }

    @Test
    void creditShouldRejectNullRequest() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> creditService.credit(1L, null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verifyNoInteractions(accountRepository, creditRepository);
    }

    @Test
    void creditShouldRejectInvalidAccountId() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> creditService.credit(0L, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verifyNoInteractions(accountRepository, creditRepository);
    }

    @Test
    void creditShouldRejectNullAmountBeforeAccessingRepositories() {
        request.setAmount(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> creditService.credit(1L, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verifyNoInteractions(accountRepository, creditRepository);
    }

    @Test
    void creditShouldRejectReferenceReusedForDifferentAccount() {
        Account account = account(2L, "100.00", "TND", "ACTIVE");
        AccountCredit previous = existingCredit(
                1L, "50.00", "TND", "CREDIT_123", "150.00");

        when(accountRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(account));
        when(creditRepository.findByReference("CREDIT_123"))
                .thenReturn(Optional.of(previous));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> creditService.credit(2L, request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals(new BigDecimal("100.00"), account.getBalance());
        verify(accountRepository, never()).save(any(Account.class));
        verify(creditRepository, never()).saveAndFlush(any(AccountCredit.class));
    }

    @Test
    void creditShouldRejectBalanceOverflow() {
        Account account = account(
                1L, "99999999999999999.00", "TND", "ACTIVE");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account));
        when(creditRepository.findByReference("CREDIT_123")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> creditService.credit(1L, request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals(new BigDecimal("99999999999999999.00"), account.getBalance());
        verify(accountRepository, never()).save(any(Account.class));
        verify(creditRepository, never()).saveAndFlush(any(AccountCredit.class));
    }
}
