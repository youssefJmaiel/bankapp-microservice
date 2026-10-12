package com.bankapp.payment.domain;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

public class CreatePaymentRequest {

    @NotNull(message = "Le compte source est obligatoire")
    @Positive(message = "L'identifiant du compte source doit être positif")
    private Long sourceAccountId;

    @NotNull(message = "Le compte destinataire est obligatoire")
    @Positive(message = "L'identifiant du compte destinataire doit être positif")
    private Long destinationAccountId;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.00", inclusive = false,
            message = "Le montant doit être supérieur à zéro")
    @Digits(integer = 17, fraction = 2,
            message = "Le montant doit comporter au maximum deux décimales")
    private BigDecimal amount;

    @NotBlank(message = "La devise est obligatoire")
    @Pattern(regexp = "^[A-Z]{3}$",
            message = "La devise doit être un code de trois lettres majuscules, par exemple TND")
    private String currency;

    // Facultative : la référence officielle est générée par le backend.
    private String reference;

    public CreatePaymentRequest() {
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
