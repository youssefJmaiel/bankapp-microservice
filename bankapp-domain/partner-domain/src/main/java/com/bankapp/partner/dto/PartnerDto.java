package com.bankapp.partner.dto;

import com.bankapp.partner.dto.PartnerDirection;
import com.bankapp.partner.dto.PartnerType;
import com.bankapp.partner.dto.ProcessedFlowType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerDto {

    private Long id;

    private String alias;

    private PartnerType type;

    private PartnerDirection direction;

    private String application;

    private ProcessedFlowType processedFlowType;

    private String description;
}
