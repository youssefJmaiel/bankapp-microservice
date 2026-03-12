package com.bankapp.mission.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long assignedEmployeeId;
    private String assignedEmployeeName; // récupéré via HR-Service si besoin
}