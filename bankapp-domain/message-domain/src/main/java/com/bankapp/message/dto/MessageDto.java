package com.bankapp.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {

    private Long id;

    private String content;

    private String sender;

    private String receiver;

    private LocalDateTime timestamp;

    private boolean processed;
}
