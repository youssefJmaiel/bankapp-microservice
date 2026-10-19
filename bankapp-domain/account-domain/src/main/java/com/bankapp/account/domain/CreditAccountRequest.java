package com.bankapp.account.domain;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public class CreditAccountRequest {

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.00", inclusive = false,
            message = "Le montant doit être supérieur à zéro")
    @Digits(integer = 17, fraction = 2,
            message = "Le montant doit comporter au maximum 17 chiffres entiers et deux décimales")
    private BigDecimal amount;

    @NotBlank(message = "La référence est obligatoire")
    @Pattern(regexp = "^[A-Za-z0-9_-]{3,100}$",
            message = "La référence doit contenir entre 3 et 100 caractères alphanumériques, tirets ou underscores")
    private String reference;

    public CreditAccountRequest() {
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
