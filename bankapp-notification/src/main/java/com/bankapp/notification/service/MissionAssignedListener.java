package com.bankapp.notification.service;

import com.bankapp.mission.dto.MissionAssignedEvent;
import com.bankapp.notification.dto.MailRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MissionAssignedListener {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @JmsListener(destination = "${ibm.mq.mission-assigned-queue}")
    public void onMissionAssigned(String payload) throws Exception {
        MissionAssignedEvent event =
                objectMapper.readValue(payload, MissionAssignedEvent.class);

        log.info("Événement MQ reçu pour la mission {}", event.getMissionTitle());

        if (event.getRecipientEmail() == null ||
                event.getRecipientEmail().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "L'événement ne contient pas d'adresse e-mail.");
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", event.getRecipientFirstName());
        variables.put("missionTitle", event.getMissionTitle());
        variables.put("missionDescription", event.getMissionDescription());
        variables.put("startDate", event.getStartDate());
        variables.put("endDate", event.getEndDate());

        MailRequest request = MailRequest.builder()
                .to(event.getRecipientEmail())
                .subject("Nouvelle mission assignée : " + event.getMissionTitle())
                .template("mission-assigned")
                .variables(variables)
                .build();

        notificationService.sendMail(request);
        log.info("E-mail envoyé pour la mission {}", event.getMissionTitle());
    }
}
