package com.bankapp.leave.converter;

import org.springframework.stereotype.Component;

import com.bankapp.leave.domain.LeaveRequestDto;
import com.bankapp.leave.entity.LeaveRequest;

@Component
public class LeaveRequestDtoConverter {

    public LeaveRequestDto convert(LeaveRequest entity) {
        if (entity == null) {
            return null;
        }

        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setId(entity.getId());
        dto.setRequesterUsername(entity.getRequesterUsername());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setReason(entity.getReason());
        dto.setStatus(entity.getStatus() == null ? null : entity.getStatus().name());
        dto.setDecisionBy(entity.getDecisionBy());
        dto.setDecisionAt(entity.getDecisionAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }
}
