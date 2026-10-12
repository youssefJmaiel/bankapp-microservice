package com.bankapp.account.domain;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

public class AccountTransferRequest {

    @NotNull(message = "Le compte source est obligatoire")
    @Positive(message = "L'identifiant du compte source doit être positif")
    private Long sourceAccountId;

    // Conservé pour la compatibilité avec les anciens clients.
    @Positive(message = "L'identifiant du compte destinataire doit être positif")
    private Long destinationAccountId;

    // Nouveau champ utilisé par le formulaire frontend.
    @Pattern(
        regexp = "^TN[0-9]{22}$",
        message = "Le numéro de compte doit commencer par TN et contenir 22 chiffres"
    )
    private String destinationAccountNumber;

    @AssertTrue(message = "Le compte destinataire est obligatoire")
    public boolean isDestinationProvided() {
        return destinationAccountId != null
                || (destinationAccountNumber != null
                    && !destinationAccountNumber.trim().isEmpty());
    }

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(
        value = "0.00",
        inclusive = false,
        message = "Le montant doit être supérieur à zéro"
    )
    @Digits(
        integer = 17,
        fraction = 2,
        message = "Le montant doit comporter au maximum deux décimales"
    )
    private BigDecimal amount;

    @NotBlank(message = "La devise est obligatoire")
    @Pattern(
        regexp = "^[A-Z]{3}$",
        message = "La devise doit être un code de trois lettres majuscules"
    )
    private String currency;

    @NotBlank(message = "La référence est obligatoire")
    @Pattern(
        regexp = "^[A-Za-z0-9_-]{3,100}$",
        message = "La référence doit contenir entre 3 et 100 caractères alphanumériques, tirets ou underscores"
    )
    private String reference;

    public AccountTransferRequest() {
    }

    public Long getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(Long sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public Long getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(Long destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    public void setDestinationAccountNumber(String destinationAccountNumber) {
        this.destinationAccountNumber = destinationAccountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
