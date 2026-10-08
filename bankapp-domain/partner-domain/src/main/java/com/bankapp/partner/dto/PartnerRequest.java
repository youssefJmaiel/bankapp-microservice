package com.bankapp.partner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerRequest {

    @NotBlank
    private String alias;

    @NotNull
    private PartnerType type;

    @NotNull
    private PartnerDirection direction;

    private String application;

    @NotNull
    private ProcessedFlowType processedFlowType;

    @NotBlank
    private String description;
}
