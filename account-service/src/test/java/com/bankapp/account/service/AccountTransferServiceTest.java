package com.bankapp.account.service;

import com.bankapp.account.domain.AccountTransferRequest;
import com.bankapp.account.entity.Account;
import com.bankapp.account.entity.AccountTransfer;
import com.bankapp.account.repository.AccountRepository;
import com.bankapp.account.repository.AccountTransferRepository;
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
class AccountTransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountTransferRepository transferRepository;

    @InjectMocks
    private AccountTransferService transferService;

    private AccountTransferRequest request;

    @BeforeEach
    void setUp() {
        request = new AccountTransferRequest();
        request.setSourceAccountId(2L);
        request.setDestinationAccountId(1L);
        request.setAmount(new BigDecimal("25.00"));
        request.setCurrency("TND");
        request.setReference("REF-123");
    }

    private Account account(
            Long id, String owner, String currency,
            String balance, String status) {
        Account account = new Account();
        account.setId(id);
        account.setOwnerId(owner);
        account.setCurrency(currency);
        account.setBalance(new BigDecimal(balance));
        account.setStatus(status);
        return account;
    }

    private void stubAccounts(Account source, Account destination) {
        // Le service doit verrouiller les comptes dans l'ordre croissant des IDs.
        when(accountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(destination));
        when(accountRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(source));
    }

    @Test
    void transferShouldDebitSourceAndCreditDestination() {
        Account source = account(2L, "user-123", "TND", "100.00", "ACTIVE");
        Account destination = account(1L, "user-456", "TND", "40.00", "ACTIVE");
        stubAccounts(source, destination);

        when(transferRepository.findByReference("REF-123"))
                .thenReturn(Optional.empty());
        when(transferRepository.saveAndFlush(any(AccountTransfer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountTransfer result =
                transferService.transfer(request, "user-123", "youssef");

        assertEquals(new BigDecimal("75.00"), source.getBalance());
        assertEquals(new BigDecimal("65.00"), destination.getBalance());
        assertEquals("REF-123", result.getReference());
        assertEquals(2L, result.getSourceAccountId());
        assertEquals(1L, result.getDestinationAccountId());
        assertEquals(new BigDecimal("25.00"), result.getAmount());

        verify(accountRepository).findByIdForUpdate(1L);
        verify(accountRepository).findByIdForUpdate(2L);
        verify(accountRepository).save(source);
        verify(accountRepository).save(destination);
        verify(transferRepository).saveAndFlush(any(AccountTransfer.class));
    }

    @Test
    void transferShouldRejectInsufficientBalance() {
        Account source = account(2L, "user-123", "TND", "10.00", "ACTIVE");
        Account destination = account(1L, "user-456", "TND", "40.00", "ACTIVE");
        stubAccounts(source, destination);
        when(transferRepository.findByReference("REF-123"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transferService.transfer(request, "user-123", "youssef"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals(new BigDecimal("10.00"), source.getBalance());
        assertEquals(new BigDecimal("40.00"), destination.getBalance());
        verify(accountRepository, never()).save(any(Account.class));
        verify(transferRepository, never()).saveAndFlush(any(AccountTransfer.class));
    }

    @Test
    void transferShouldRejectWhenSourceDoesNotBelongToUser() {
        Account source = account(2L, "another-user", "TND", "100.00", "ACTIVE");
        Account destination = account(1L, "user-456", "TND", "40.00", "ACTIVE");
        stubAccounts(source, destination);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transferService.transfer(request, "user-123", "youssef"));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verify(transferRepository, never()).saveAndFlush(any(AccountTransfer.class));
    }

    @Test
    void transferShouldAcceptUsernameWhenItMatchesSourceOwner() {
        Account source = account(2L, "youssef", "TND", "100.00", "ACTIVE");
        Account destination = account(1L, "user-456", "TND", "40.00", "ACTIVE");
        stubAccounts(source, destination);
        when(transferRepository.findByReference("REF-123"))
                .thenReturn(Optional.empty());
        when(transferRepository.saveAndFlush(any(AccountTransfer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountTransfer result =
                transferService.transfer(request, "different-user", "youssef");

        assertNotNull(result);
        assertEquals(new BigDecimal("75.00"), source.getBalance());
    }

    @Test
    void transferShouldRejectInactiveAccount() {
        Account source = account(2L, "user-123", "TND", "100.00", "BLOCKED");
        Account destination = account(1L, "user-456", "TND", "40.00", "ACTIVE");
        stubAccounts(source, destination);
        when(transferRepository.findByReference("REF-123"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transferService.transfer(request, "user-123", "youssef"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void transferShouldRejectCurrencyMismatch() {
        Account source = account(2L, "user-123", "TND", "100.00", "ACTIVE");
        Account destination = account(1L, "user-456", "EUR", "40.00", "ACTIVE");
        stubAccounts(source, destination);
        when(transferRepository.findByReference("REF-123"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transferService.transfer(request, "user-123", "youssef"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void transferShouldReturnExistingTransferForSameReferenceAndDetails() {
        Account source = account(2L, "user-123", "TND", "100.00", "ACTIVE");
        Account destination = account(1L, "user-456", "TND", "40.00", "ACTIVE");
        stubAccounts(source, destination);

        AccountTransfer existing = new AccountTransfer();
        existing.setReference("REF-123");
        existing.setSourceAccountId(2L);
        existing.setDestinationAccountId(1L);
        existing.setAmount(new BigDecimal("25.00"));
        existing.setCurrency("TND");

        when(transferRepository.findByReference("REF-123"))
                .thenReturn(Optional.of(existing));

        AccountTransfer result =
                transferService.transfer(request, "user-123", "youssef");

        assertSame(existing, result);
        assertEquals(new BigDecimal("100.00"), source.getBalance());
        assertEquals(new BigDecimal("40.00"), destination.getBalance());
        verify(accountRepository, never()).save(any(Account.class));
        verify(transferRepository, never()).saveAndFlush(any(AccountTransfer.class));
    }

    @Test
    void transferShouldRejectReusedReferenceWithDifferentAmount() {
        Account source = account(2L, "user-123", "TND", "100.00", "ACTIVE");
        Account destination = account(1L, "user-456", "TND", "40.00", "ACTIVE");
        stubAccounts(source, destination);

        AccountTransfer existing = new AccountTransfer();
        existing.setReference("REF-123");
        existing.setSourceAccountId(2L);
        existing.setDestinationAccountId(1L);
        existing.setAmount(new BigDecimal("50.00"));
        existing.setCurrency("TND");

        when(transferRepository.findByReference("REF-123"))
                .thenReturn(Optional.of(existing));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transferService.transfer(request, "user-123", "youssef"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void transferShouldRejectNullSourceIdWithBadRequest() {
        request.setSourceAccountId(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transferService.transfer(request, "user-123", "youssef"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(accountRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void transferShouldRejectNullCurrencyWithBadRequest() {
        request.setCurrency(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transferService.transfer(request, "user-123", "youssef"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(accountRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void transferShouldRejectNonPositiveAmountWithBadRequest() {
        request.setAmount(BigDecimal.ZERO);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transferService.transfer(request, "user-123", "youssef"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(accountRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void transferShouldRejectReferenceWithInvalidFormat() {
        request.setReference("bad reference!");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transferService.transfer(request, "user-123", "youssef"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(accountRepository, never()).findByIdForUpdate(any());
    }


    @Test
    void transferShouldRejectNullRequest() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transferService.transfer(null, "user-123", "youssef"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verifyNoInteractions(accountRepository, transferRepository);
    }

    @Test
    void transferShouldRejectAmountWithMoreThanTwoDecimals() {
        request.setAmount(new BigDecimal("25.001"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transferService.transfer(request, "user-123", "youssef"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verifyNoInteractions(accountRepository, transferRepository);
    }

    @Test
    void transferShouldRejectUnknownDestinationAccountNumber() {
        request.setDestinationAccountId(null);
        request.setDestinationAccountNumber("TN1234567890123456789012");
        when(accountRepository.findByAccountNumber("TN1234567890123456789012"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transferService.transfer(request, "user-123", "youssef"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(accountRepository, never()).findByIdForUpdate(any());
        verify(accountRepository, never()).save(any(Account.class));
        verify(transferRepository, never()).saveAndFlush(any(AccountTransfer.class));
    }

    @Test
    void transferShouldRejectMismatchedDestinationIdAndNumber() {
        request.setDestinationAccountId(1L);
        request.setDestinationAccountNumber("TN1234567890123456789012");

        Account accountWithNumber = account(
                3L, "user-456", "TND", "40.00", "ACTIVE");
        when(accountRepository.findByAccountNumber("TN1234567890123456789012"))
                .thenReturn(Optional.of(accountWithNumber));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transferService.transfer(request, "user-123", "youssef"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(accountRepository, never()).findByIdForUpdate(any());
        verify(accountRepository, never()).save(any(Account.class));
        verify(transferRepository, never()).saveAndFlush(any(AccountTransfer.class));
    }

    @Test
    void transferShouldRejectTransferToSameAccount() {
        request.setDestinationAccountId(2L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transferService.transfer(request, "user-123", "youssef"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verifyNoInteractions(accountRepository, transferRepository);
    }


    @Test
    void transferShouldRejectReusedReferenceWithDifferentDestination() {
        Account source = account(2L, "user-123", "TND", "100.00", "ACTIVE");
        Account destination = account(1L, "user-456", "TND", "40.00", "ACTIVE");
        stubAccounts(source, destination);

        AccountTransfer existing = new AccountTransfer();
        existing.setReference("REF-123");
        existing.setSourceAccountId(2L);
        existing.setDestinationAccountId(3L);
        existing.setAmount(new BigDecimal("25.00"));
        existing.setCurrency("TND");

        when(transferRepository.findByReference("REF-123"))
                .thenReturn(Optional.of(existing));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transferService.transfer(request, "user-123", "youssef"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(accountRepository, never()).save(any(Account.class));
        verify(transferRepository, never()).saveAndFlush(any(AccountTransfer.class));
    }

    @Test
    void transferShouldRejectReusedReferenceWithDifferentCurrency() {
        Account source = account(2L, "user-123", "TND", "100.00", "ACTIVE");
        Account destination = account(1L, "user-456", "TND", "40.00", "ACTIVE");
        stubAccounts(source, destination);

        AccountTransfer existing = new AccountTransfer();
        existing.setReference("REF-123");
        existing.setSourceAccountId(2L);
        existing.setDestinationAccountId(1L);
        existing.setAmount(new BigDecimal("25.00"));
        existing.setCurrency("EUR");

        when(transferRepository.findByReference("REF-123"))
                .thenReturn(Optional.of(existing));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transferService.transfer(request, "user-123", "youssef"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(accountRepository, never()).save(any(Account.class));
        verify(transferRepository, never()).saveAndFlush(any(AccountTransfer.class));
    }

    @Test
    void transferShouldRejectInactiveDestinationAccount() {
        Account source = account(2L, "user-123", "TND", "100.00", "ACTIVE");
        Account destination = account(1L, "user-456", "TND", "40.00", "BLOCKED");
        stubAccounts(source, destination);
        when(transferRepository.findByReference("REF-123"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transferService.transfer(request, "user-123", "youssef"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(accountRepository, never()).save(any(Account.class));
        verify(transferRepository, never()).saveAndFlush(any(AccountTransfer.class));
    }

    @Test
    void transferShouldReturnNotFoundWhenSourceAccountDoesNotExist() {
        when(accountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(account(
                        1L, "user-456", "TND", "40.00", "ACTIVE")));
        when(accountRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transferService.transfer(request, "user-123", "youssef"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(accountRepository, never()).save(any(Account.class));
        verify(transferRepository, never()).saveAndFlush(any(AccountTransfer.class));
    }

    @Test
    void transferShouldReturnNotFoundWhenDestinationAccountDoesNotExist() {
        when(accountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transferService.transfer(request, "user-123", "youssef"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(accountRepository).findByIdForUpdate(1L);
        verify(accountRepository, never()).save(any(Account.class));
        verify(transferRepository, never()).saveAndFlush(any(AccountTransfer.class));
    }

    @Test
    void transferShouldRejectNegativeDestinationId() {
        request.setDestinationAccountId(-1L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> transferService.transfer(request, "user-123", "youssef"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verifyNoInteractions(accountRepository, transferRepository);
    }
}
