package com.bankapp.account.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class CreateAccountRequest {

    @NotBlank(message = "L'identifiant du propriétaire est obligatoire")
    @Pattern(
        regexp = "^[A-Za-z0-9._:@-]{1,100}$",
        message = "L'identifiant du propriétaire contient des caractères invalides"
    )
    private String ownerId;

    @NotBlank(message = "La devise est obligatoire")
    @Pattern(
        regexp = "^[A-Z]{3}$",
        message = "La devise doit être un code de trois lettres majuscules, par exemple TND"
    )
    private String currency = "TND";

    public CreateAccountRequest() {
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
