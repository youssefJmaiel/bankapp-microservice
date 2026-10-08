package com.bankapp.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRequest {

    @NotBlank(message = "Content must not be blank")
    @Size(min = 1, max = 500, message = "Content must be between 1 and 500 characters")
    private String content;

    @NotBlank(message = "Sender must not be blank")
    private String sender;

    @NotBlank(message = "Receiver must not be blank")
    private String receiver;
}
