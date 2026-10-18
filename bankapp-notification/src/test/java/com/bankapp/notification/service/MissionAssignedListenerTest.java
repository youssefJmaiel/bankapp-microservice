package com.bankapp.notification.service;

import com.bankapp.mission.dto.MissionAssignedEvent;
import com.bankapp.notification.dto.MailRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MissionAssignedListenerTest {

    private ObjectMapper objectMapper;
    private NotificationService notificationService;
    private MissionAssignedListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        notificationService = mock(NotificationService.class);
        listener = new MissionAssignedListener(objectMapper, notificationService);
    }

    @Test
    void onMissionAssignedShouldBuildAndSendMailRequest() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 11, 2, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 11, 6, 17, 0);

        MissionAssignedEvent event = MissionAssignedEvent.builder()
                .eventId(UUID.randomUUID())
                .occurredAt(LocalDateTime.of(2026, 10, 18, 10, 0))
                .missionId(42L)
                .missionTitle("Migration bancaire")
                .missionDescription("Migrer les comptes vers le nouveau système")
                .startDate(start)
                .endDate(end)
                .assignedEmployeeId(7L)
                .recipientEmail("employee@example.com")
                .recipientFirstName("Youssef")
                .build();

        listener.onMissionAssigned(objectMapper.writeValueAsString(event));

        ArgumentCaptor<MailRequest> captor = ArgumentCaptor.forClass(MailRequest.class);
        verify(notificationService).sendMail(captor.capture());

        MailRequest request = captor.getValue();
        assertEquals("employee@example.com", request.getTo());
        assertEquals("Nouvelle mission assignée : Migration bancaire", request.getSubject());
        assertEquals("mission-assigned", request.getTemplate());
        assertNotNull(request.getVariables());
        assertEquals("Youssef", request.getVariables().get("firstName"));
        assertEquals("Migration bancaire", request.getVariables().get("missionTitle"));
        assertEquals("Migrer les comptes vers le nouveau système",
                request.getVariables().get("missionDescription"));
        assertEquals(start, request.getVariables().get("startDate"));
        assertEquals(end, request.getVariables().get("endDate"));
    }

    @Test
    void onMissionAssignedShouldRejectNullRecipientEmail() throws Exception {
        MissionAssignedEvent event = MissionAssignedEvent.builder()
                .missionTitle("Mission sans destinataire")
                .recipientEmail(null)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> listener.onMissionAssigned(objectMapper.writeValueAsString(event)));

        verifyNoInteractions(notificationService);
    }

    @Test
    void onMissionAssignedShouldRejectBlankRecipientEmail() throws Exception {
        MissionAssignedEvent event = MissionAssignedEvent.builder()
                .missionTitle("Mission sans destinataire")
                .recipientEmail("   ")
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> listener.onMissionAssigned(objectMapper.writeValueAsString(event)));

        verifyNoInteractions(notificationService);
    }

    @Test
    void onMissionAssignedShouldPropagateInvalidJson() {
        assertThrows(Exception.class, () -> listener.onMissionAssigned("{invalid-json"));
        verifyNoInteractions(notificationService);
    }
}
