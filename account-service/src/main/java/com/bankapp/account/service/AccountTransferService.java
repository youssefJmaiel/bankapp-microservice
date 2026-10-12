package com.bankapp.account.service;

import com.bankapp.account.domain.AccountTransferRequest;
import com.bankapp.account.entity.Account;
import com.bankapp.account.entity.AccountTransfer;
import com.bankapp.account.repository.AccountRepository;
import com.bankapp.account.repository.AccountTransferRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class AccountTransferService {

    private final AccountRepository accountRepository;
    private final AccountTransferRepository transferRepository;

    public AccountTransferService(
            AccountRepository accountRepository,
            AccountTransferRepository transferRepository) {
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
    }

    @Transactional
    public AccountTransfer transfer(
            AccountTransferRequest request,
            String userId,
            String username) {

        Long sourceId = request.getSourceAccountId();

        Long destinationId;
        if (request.getDestinationAccountNumber() != null
                && !request.getDestinationAccountNumber().trim().isEmpty()) {
            String accountNumber = request.getDestinationAccountNumber().trim();

            Account destinationAccount = accountRepository
                    .findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Compte destinataire introuvable"));

            destinationId = destinationAccount.getId();

            if (request.getDestinationAccountId() != null
                    && !request.getDestinationAccountId().equals(destinationId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Le numéro et l'identifiant du compte destinataire ne correspondent pas");
            }
        } else {
            destinationId = request.getDestinationAccountId();
        }

        if (destinationId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le numéro du compte destinataire est obligatoire");
        }

        BigDecimal amount = request.getAmount();
        String currency = request.getCurrency();
        String reference = request.getReference();

        if (sourceId.equals(destinationId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Les comptes source et destinataire doivent être différents");
        }

        // Verrouiller les comptes dans l'ordre des identifiants pour
        // limiter les risques de blocage lors de virements simultanés.
        Account first = accountRepository.findByIdForUpdate(
                Math.min(sourceId, destinationId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Compte introuvable"));

        Account second = accountRepository.findByIdForUpdate(
                Math.max(sourceId, destinationId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Compte introuvable"));

        Account source = sourceId.equals(first.getId()) ? first : second;
        Account destination =
                destinationId.equals(first.getId()) ? first : second;

        boolean ownsSource =
                Objects.equals(source.getOwnerId(), userId)
                || (username != null
                    && Objects.equals(source.getOwnerId(), username));

        if (!ownsSource) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Vous ne pouvez pas effectuer un virement depuis ce compte");
        }

        // Une référence déjà utilisée ne doit jamais provoquer un second débit.
        AccountTransfer previous = transferRepository
                .findByReference(reference)
                .orElse(null);

        if (previous != null) {
            boolean sameTransfer =
                    Objects.equals(previous.getSourceAccountId(), sourceId)
                    && Objects.equals(previous.getDestinationAccountId(), destinationId)
                    && previous.getAmount().compareTo(amount) == 0
                    && Objects.equals(previous.getCurrency(), currency);

            if (!sameTransfer) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Cette référence est déjà utilisée pour un autre virement");
            }

            return previous;
        }

        if (!"ACTIVE".equals(source.getStatus())
                || !"ACTIVE".equals(destination.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Les deux comptes doivent être actifs");
        }

        if (!currency.equals(source.getCurrency())
                || !currency.equals(destination.getCurrency())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La devise du virement doit correspondre à celle des deux comptes");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0
                || amount.scale() > 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Le montant doit être positif et comporter au maximum deux décimales");
        }

        if (source.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solde insuffisant");
        }

        // Les deux comptes et l'historique sont modifiés dans la même transaction.
        source.setBalance(source.getBalance().subtract(amount));
        destination.setBalance(destination.getBalance().add(amount));

        accountRepository.save(source);
        accountRepository.save(destination);

        AccountTransfer transfer = new AccountTransfer();
        transfer.setReference(reference);
        transfer.setSourceAccountId(sourceId);
        transfer.setDestinationAccountId(destinationId);
        transfer.setAmount(amount);
        transfer.setCurrency(currency);

        return transferRepository.saveAndFlush(transfer);
    }
}
