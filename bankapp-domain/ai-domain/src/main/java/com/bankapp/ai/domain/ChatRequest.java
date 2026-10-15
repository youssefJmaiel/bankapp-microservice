package com.bankapp.ai.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ChatRequest {

    @NotBlank(message = "Le message ne doit pas être vide.")
    @Size(max = 2000, message = "Le message ne doit pas dépasser 2000 caractères.")
    private String message;

    public ChatRequest() {
    }

    public ChatRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
