package com.bankapp.account.service;

import com.bankapp.account.domain.AccountCreditDto;
import com.bankapp.account.domain.CreditAccountRequest;
import com.bankapp.account.entity.Account;
import com.bankapp.account.entity.AccountCredit;
import com.bankapp.account.repository.AccountCreditRepository;
import com.bankapp.account.repository.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class AccountCreditService {

    private final AccountRepository accountRepository;
    private final AccountCreditRepository creditRepository;

    public AccountCreditService(
            AccountRepository accountRepository,
            AccountCreditRepository creditRepository) {
        this.accountRepository = accountRepository;
        this.creditRepository = creditRepository;
    }

    @Transactional
    public AccountCreditDto credit(Long accountId, CreditAccountRequest request) {
        if (accountId == null || accountId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "L'identifiant du compte doit être positif");
        }
        if (request == null || request.getAmount() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Le montant est obligatoire");
        }

        BigDecimal amount = request.getAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0
                || amount.scale() > 2
                || amount.precision() - amount.scale() > 17) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le montant doit être positif et comporter au maximum 17 chiffres entiers et deux décimales");
        }

        String reference = request.getReference();
        if (reference == null || !reference.matches("^[A-Za-z0-9_-]{3,100}$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La référence doit contenir entre 3 et 100 caractères alphanumériques, tirets ou underscores");
        }

        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Compte introuvable"));

        AccountCredit previous = creditRepository.findByReference(reference).orElse(null);
        if (previous != null) {
            boolean sameCredit = Objects.equals(previous.getAccountId(), accountId)
                    && previous.getAmount().compareTo(amount) == 0
                    && Objects.equals(previous.getCurrency(), account.getCurrency());

            if (!sameCredit) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Cette référence est déjà utilisée pour un autre crédit");
            }
            return toDto(previous);
        }

        if (!"ACTIVE".equals(account.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Seuls les comptes actifs peuvent être alimentés");
        }

        BigDecimal newBalance = account.getBalance().add(amount);
        if (newBalance.precision() - newBalance.scale() > 17) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Le solde obtenu dépasse la limite autorisée");
        }

        account.setBalance(newBalance);
        accountRepository.save(account);

        AccountCredit credit = new AccountCredit();
        credit.setAccountId(accountId);
        credit.setAmount(amount);
        credit.setCurrency(account.getCurrency());
        credit.setReference(reference);
        credit.setBalanceAfter(newBalance);

        return toDto(creditRepository.saveAndFlush(credit));
    }

    private AccountCreditDto toDto(AccountCredit credit) {
        AccountCreditDto dto = new AccountCreditDto();
        dto.setId(credit.getId());
        dto.setAccountId(credit.getAccountId());
        dto.setAmount(credit.getAmount());
        dto.setCurrency(credit.getCurrency());
        dto.setReference(credit.getReference());
        dto.setBalanceAfter(credit.getBalanceAfter());
        dto.setCreatedAt(credit.getCreatedAt());
        return dto;
    }
}
