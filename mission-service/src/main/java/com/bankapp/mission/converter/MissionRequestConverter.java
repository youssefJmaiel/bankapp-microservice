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

        Mission mission = new Mission();
        mission.setTitle(request.getTitle());
        mission.setDescription(request.getDescription());
        mission.setStartDate(request.getStartDate());
        mission.setEndDate(request.getEndDate());
        mission.setAssignedEmployeeId(request.getAssignedEmployeeId());
        mission.setLocation(request.getLocation());
        mission.setStatus(
                request.getStatus() == null || request.getStatus().isBlank()
                        ? "PENDING" : request.getStatus()
        );
        mission.setPriority(
                request.getPriority() == null || request.getPriority().isBlank()
                        ? "MEDIUM" : request.getPriority()
        );
        mission.setBudget(request.getBudget());

        return mission;
    }
}
