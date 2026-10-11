package com.bankapp.mission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @NotNull
    private Long assignedEmployeeId;

    private String location;

    private String status;

    private String priority;

    private BigDecimal budget;
}
