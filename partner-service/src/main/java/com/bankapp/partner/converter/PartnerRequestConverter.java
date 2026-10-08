package com.bankapp.partner.converter;

import com.bankapp.partner.dto.PartnerRequest;
import com.bankapp.partner.entity.Partner;
import org.springframework.stereotype.Component;

@Component
public class PartnerRequestConverter {

    public Partner convert(PartnerRequest request) {
        if (request == null) {
            return null;
        }

        return Partner.builder()
                .alias(request.getAlias())
                .type(request.getType())
                .direction(request.getDirection())
                .application(request.getApplication())
                .processedFlowType(request.getProcessedFlowType())
                .description(request.getDescription())
                .build();
    }
}
