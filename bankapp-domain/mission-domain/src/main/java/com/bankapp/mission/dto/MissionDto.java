package com.bankapp.mission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionDto {

    private Long id;

    private String title;

    private String description;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Long assignedEmployeeId;

    private String assignedEmployeeName;

    private String location;

    private String status;

    private String priority;

    private BigDecimal budget;
}
