package com.bankapp.mission.converter;

import com.bankapp.mission.dto.MissionDto;
import com.bankapp.mission.entity.Mission;
import org.springframework.stereotype.Component;

@Component
public class MissionDtoConverter {

    public MissionDto convert(Mission mission) {

        if (mission == null) {
            return null;
        }

        return MissionDto.builder()
                .id(mission.getId())
                .title(mission.getTitle())
                .description(mission.getDescription())
                .startDate(mission.getStartDate())
                .endDate(mission.getEndDate())
                .assignedEmployeeId(mission.getAssignedEmployeeId())
                .build();
    }
}
