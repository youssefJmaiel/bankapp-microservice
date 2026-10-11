package com.bankapp.mission.messaging;

import com.bankapp.mission.dto.MissionAssignedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MissionEventPublisher {

    private static final String DESTINATION = "MISSION.ASSIGNED";

    private final JmsTemplate missionJmsTemplate;
    private final ObjectMapper objectMapper;

    public void publishMissionAssigned(MissionAssignedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            missionJmsTemplate.convertAndSend(DESTINATION, payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Impossible de convertir l'événement de mission en JSON", e);
        }
    }
}
