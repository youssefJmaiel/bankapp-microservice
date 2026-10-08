package com.bankapp.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailDto {

    private Long id;

    private String to;

    private String subject;

    private String template;

    private String locale;

    private String status;
}
