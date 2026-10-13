package com.bankapp.leave.converter;

import org.springframework.stereotype.Component;

import com.bankapp.leave.domain.CreateLeaveRequest;
import com.bankapp.leave.entity.LeaveRequest;

@Component
public class LeaveRequestConverter {

    public LeaveRequest convert(CreateLeaveRequest request) {
        if (request == null) {
            return null;
        }

        LeaveRequest entity = new LeaveRequest();
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setReason(request.getReason() == null ? null : request.getReason().trim());
        entity.setStatus(LeaveRequest.Status.PENDING);

        return entity;
    }
}
