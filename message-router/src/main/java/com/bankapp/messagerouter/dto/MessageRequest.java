package com.bankapp.messagerouter.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
@Data
public class MessageRequest {

    @NotNull(message = "Content must not be null")
    @Size(min = 1, max = 500, message = "Content must be between 1 and 500 characters")
    private String content;

    @NotNull(message = "Sender must not be null")
    private String sender;

    @NotNull(message = "Receiver must not be null")
    private String receiver;

    // Getters and Setters
}
