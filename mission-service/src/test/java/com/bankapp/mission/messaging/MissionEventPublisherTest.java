package com.bankapp.mission.messaging;

import com.bankapp.mission.dto.MissionAssignedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MissionEventPublisherTest {

    private static final String DESTINATION = "MISSION.ASSIGNED";

    @Mock
    private JmsTemplate jmsTemplate;

    private ObjectMapper objectMapper;
    private MissionEventPublisher publisher;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        publisher = new MissionEventPublisher(jmsTemplate, objectMapper);
    }

    @Test
    void publishesSerializedEventToMissionAssignedQueue() throws Exception {
        UUID eventId = UUID.randomUUID();

        MissionAssignedEvent event = MissionAssignedEvent.builder()
                .eventId(eventId)
                .occurredAt(LocalDateTime.of(2026, 10, 24, 0, 30))
                .missionId(10L)
                .missionTitle("Migration bancaire")
                .missionDescription("Migration technique")
                .startDate(LocalDateTime.of(2026, 11, 1, 9, 0))
                .endDate(LocalDateTime.of(2026, 11, 5, 17, 0))
                .assignedEmployeeId(42L)
                .recipientEmail("employee@example.com")
                .recipientFirstName("Test")
                .build();

        publisher.publishMissionAssigned(event);

        ArgumentCaptor<String> payloadCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(jmsTemplate).convertAndSend(
                eq(DESTINATION), payloadCaptor.capture());

        MissionAssignedEvent actual = objectMapper.readValue(
                payloadCaptor.getValue(), MissionAssignedEvent.class);

        assertEquals(eventId, actual.getEventId());
        assertEquals(10L, actual.getMissionId());
        assertEquals("Migration bancaire", actual.getMissionTitle());
        assertEquals("Migration technique", actual.getMissionDescription());
        assertEquals(42L, actual.getAssignedEmployeeId());
        assertEquals("employee@example.com", actual.getRecipientEmail());
        assertEquals("Test", actual.getRecipientFirstName());
        assertEquals(event.getStartDate(), actual.getStartDate());
        assertEquals(event.getEndDate(), actual.getEndDate());
        verifyNoMoreInteractions(jmsTemplate);
    }

    @Test
    void throwsIllegalStateExceptionWhenSerializationFails() throws Exception {
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        MissionEventPublisher failingPublisher =
                new MissionEventPublisher(jmsTemplate, failingMapper);

        MissionAssignedEvent event = MissionAssignedEvent.builder()
                .missionId(10L)
                .missionTitle("Migration bancaire")
                .build();

        when(failingMapper.writeValueAsString(event))
                .thenThrow(new JsonProcessingException("Erreur de test") {});

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> failingPublisher.publishMissionAssigned(event));

        assertEquals(
                "Impossible de convertir l'événement de mission en JSON",
                exception.getMessage());

        verify(jmsTemplate, never()).convertAndSend(
                anyString(), anyString());
    }
}
