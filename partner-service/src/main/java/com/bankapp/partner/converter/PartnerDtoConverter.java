package com.bankapp.partner.converter;

import com.bankapp.partner.dto.PartnerDto;
import com.bankapp.partner.entity.Partner;
import org.springframework.stereotype.Component;

@Component
public class PartnerDtoConverter {

    public PartnerDto convert(Partner partner) {
        if (partner == null) {
            return null;
        }

        return PartnerDto.builder()
                .id(partner.getId())
                .alias(partner.getAlias())
                .type(partner.getType())
                .direction(partner.getDirection())
                .application(partner.getApplication())
                .processedFlowType(partner.getProcessedFlowType())
                .description(partner.getDescription())
                .build();
    }
}
