package com.bankapp.mission.converter;

import com.bankapp.mission.dto.MissionRequest;
import com.bankapp.mission.entity.Mission;
import org.springframework.stereotype.Component;

@Component
public class MissionRequestConverter {

    public Mission convert(MissionRequest request) {

        if (request == null) {
            return null;
        }

        return new Mission(
                null,
                request.getTitle(),
                request.getDescription(),
                request.getStartDate(),
                request.getEndDate(),
                request.getAssignedEmployeeId()
        );
    }
}
