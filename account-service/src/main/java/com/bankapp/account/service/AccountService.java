package com.bankapp.account.service;

import com.bankapp.account.domain.AccountDto;
import com.bankapp.account.domain.CreateAccountRequest;
import com.bankapp.account.entity.Account;
import com.bankapp.account.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private static final String ACCOUNT_PREFIX = "TN";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountDto createAccount(CreateAccountRequest request) {
        Account account = new Account();
        account.setAccountNumber(generateUniqueAccountNumber());
        account.setOwnerId(request.getOwnerId());
        account.setCurrency(request.getCurrency() == null
                ? "TND"
                : request.getCurrency());
        account.setBalance(BigDecimal.ZERO);
        account.setStatus("ACTIVE");

        return toDto(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public AccountDto getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() ->
                        new AccountNotFoundException("Compte introuvable : " + id));

        return toDto(account);
    }

    @Transactional(readOnly = true)
    public AccountDto getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Compte introuvable : " + accountNumber));

        return toDto(account);
    }

    @Transactional(readOnly = true)
    public List<AccountDto> getAccountsByOwner(String ownerId) {
        return accountRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;

        do {
            StringBuilder number = new StringBuilder(ACCOUNT_PREFIX);

            for (int i = 0; i < 22; i++) {
                number.append(RANDOM.nextInt(10));
            }

            accountNumber = number.toString();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    private AccountDto toDto(Account account) {
        return AccountDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .ownerId(account.getOwnerId())
                .currency(account.getCurrency())
                .balance(account.getBalance())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
