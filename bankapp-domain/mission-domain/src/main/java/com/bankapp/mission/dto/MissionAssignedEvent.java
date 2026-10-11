package com.bankapp.mission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Événement publié lorsqu'une mission est assignée à un employé.
 * Ce contrat est partagé entre mission-service et bankapp-notification.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionAssignedEvent {

    private UUID eventId;

    private LocalDateTime occurredAt;

    private Long missionId;

    private String missionTitle;

    private String missionDescription;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Long assignedEmployeeId;

    private String recipientEmail;

    private String recipientFirstName;
}
