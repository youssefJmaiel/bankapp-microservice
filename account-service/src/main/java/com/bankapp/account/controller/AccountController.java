package com.bankapp.account.controller;

import com.bankapp.account.domain.AccountDto;
import com.bankapp.account.domain.CreateAccountRequest;
import com.bankapp.account.domain.AccountTransferRequest;
import com.bankapp.account.entity.AccountTransfer;
import com.bankapp.account.service.AccountTransferService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import com.bankapp.account.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final AccountTransferService accountTransferService;

    public AccountController(AccountService accountService,
                             AccountTransferService accountTransferService) {
        this.accountService = accountService;
        this.accountTransferService = accountTransferService;
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<AccountDto> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        requireOwnIdentityOrAdmin(request.getOwnerId(), jwt);
        AccountDto createdAccount = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/transfers")
    public ResponseEntity<AccountTransfer> transfer(
            @Valid @RequestBody AccountTransferRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");

        AccountTransfer transfer = accountTransferService.transfer(
                request, userId, username);

        return ResponseEntity.ok(transfer);
    }

    @GetMapping("/my")
    public ResponseEntity<List<AccountDto>> getMyAccounts(
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");

        List<AccountDto> accounts = accountService.getAccountsByOwner(userId);

        if (username != null && !username.equals(userId)) {
            List<AccountDto> usernameAccounts =
                    accountService.getAccountsByOwner(username);

            for (AccountDto account : usernameAccounts) {
                if (accounts.stream().noneMatch(existing ->
                        existing.getId().equals(account.getId()))) {
                    accounts.add(account);
                }
            }
        }

        return ResponseEntity.ok(accounts);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/my/transfers")
    public ResponseEntity<List<AccountTransfer>> getMyTransfers(
            @AuthenticationPrincipal Jwt jwt) {

        List<AccountDto> accounts = getMyAccounts(jwt).getBody();
        if (accounts == null || accounts.isEmpty()) {
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }

        List<Long> accountIds = accounts.stream()
                .map(AccountDto::getId)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(
                accountTransferService.findTransfersForAccounts(accountIds));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccountById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        AccountDto account = accountService.getAccountById(id);
        requireOwnIdentityOrAdmin(account.getOwnerId(), jwt);
        return ResponseEntity.ok(account);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountDto> getAccountByNumber(
            @PathVariable String accountNumber,
            @AuthenticationPrincipal Jwt jwt) {
        AccountDto account = accountService.getAccountByNumber(accountNumber);
        requireOwnIdentityOrAdmin(account.getOwnerId(), jwt);
        return ResponseEntity.ok(account);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<AccountDto>> getAccountsByOwner(
            @PathVariable String ownerId,
            @AuthenticationPrincipal Jwt jwt) {
        requireOwnIdentityOrAdmin(ownerId, jwt);
        return ResponseEntity.ok(accountService.getAccountsByOwner(ownerId));
    }

    private void requireOwnIdentityOrAdmin(String ownerId, Jwt jwt) {
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));

        if (isAdmin) {
            return;
        }

        String subject = jwt != null ? jwt.getSubject() : null;
        String username = jwt != null ? jwt.getClaimAsString("preferred_username") : null;

        boolean isOwner = ownerId != null
                && (ownerId.equals(subject) || ownerId.equals(username));

        if (!isOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to access this account");
        }
    }
}
