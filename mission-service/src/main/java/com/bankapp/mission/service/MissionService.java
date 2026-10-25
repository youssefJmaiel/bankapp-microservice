package com.bankapp.mission.service;

import com.bankapp.mission.client.HrClient;
import com.bankapp.mission.client.NotificationClient;
import com.bankapp.mission.dto.MissionAssignedEvent;
import com.bankapp.mission.messaging.MissionEventPublisher;
import com.bankapp.notification.dto.MailRequest;
import com.bankapp.mission.converter.MissionDtoConverter;
import com.bankapp.mission.converter.MissionRequestConverter;
import com.bankapp.mission.dto.MissionDto;
import com.bankapp.mission.entity.Mission;
import com.bankapp.mission.dto.MissionRequest;
import com.bankapp.hr.dto.EmployeeDto;
import com.bankapp.mission.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.beans.factory.annotation.Value;

import javax.transaction.Transactional;
import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionService {

    private static final Logger LOGGER = Logger.getLogger(MissionService.class.getName());
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final MissionRepository missionRepository;
    private final HrClient hrClient;
    private final NotificationClient notificationClient;
    private final MissionEventPublisher missionEventPublisher;

    @Value("${mission.notifications.mq-enabled:false}")
    private boolean mqNotificationsEnabled;
    private final MissionRequestConverter missionRequestConverter;
    private final MissionDtoConverter missionDtoConverter;

    public List<MissionDto> getAllMissions() {
        return missionRepository.findAll()
                .stream()
                .map(missionDtoConverter::convert)
                .collect(Collectors.toList());
    }

    /**
     * Liste toutes les missions pour un administrateur, ou uniquement celles
     * affectées à l'employé correspondant à l'identité Keycloak.
     */
    public List<MissionDto> getMissionsForUser(String username, boolean admin) {
        if (admin) {
            return getAllMissions();
        }

        Long employeeId = resolveEmployeeId(username);
        if (employeeId == null) {
            return java.util.Collections.emptyList();
        }

        return missionRepository.findByAssignedEmployeeId(employeeId)
                .stream()
                .map(missionDtoConverter::convert)
                .collect(Collectors.toList());
    }

    /**
     * Empêche un utilisateur de consulter une mission affectée à un autre
     * employé, même s'il connaît son identifiant.
     */
    public MissionDto getMissionByIdForUser(Long id, String username, boolean admin) {
        MissionDto mission = getMissionById(id);

        if (admin) {
            return mission;
        }

        Long employeeId = resolveEmployeeId(username);
        if (employeeId == null
                || !employeeId.equals(mission.getAssignedEmployeeId())) {
            throw new AccessDeniedException("Accès interdit à cette mission.");
        }

        return mission;
    }

    private Long resolveEmployeeId(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }

        return hrClient.getAllEmployees()
                .stream()
                .filter(employee -> employee.getEmail() != null)
                .filter(employee -> employee.getEmail().equalsIgnoreCase(username))
                .map(EmployeeDto::getId)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public MissionDto getMissionById(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Mission not found with id: " + id)
                );

        return missionDtoConverter.convert(mission);
    }

    public MissionDto createMission(MissionRequest request) {
        Mission mission = missionRequestConverter.convert(request);

        Mission savedMission = missionRepository.save(mission);

        if (savedMission.getAssignedEmployeeId() != null
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendMissionAssignedEmail(savedMission);
                }
            });
        }

        return missionDtoConverter.convert(savedMission);
    }

    private void sendMissionAssignedEmail(Mission mission) {
        try {
            EmployeeDto employee = hrClient.getEmployeeById(mission.getAssignedEmployeeId());

            if (employee == null || employee.getEmail() == null || employee.getEmail().isBlank()) {
                LOGGER.warning("Notification non envoyée : adresse e-mail de l'employé absente pour la mission "
                        + mission.getId());
                return;
            }

            if (mqNotificationsEnabled) {
                MissionAssignedEvent event = MissionAssignedEvent.builder()
                        .eventId(UUID.randomUUID())
                        .occurredAt(LocalDateTime.now())
                        .missionId(mission.getId())
                        .missionTitle(mission.getTitle() == null ? "" : mission.getTitle())
                        .missionDescription(mission.getDescription() == null ? "" : mission.getDescription())
                        .startDate(mission.getStartDate())
                        .endDate(mission.getEndDate())
                        .assignedEmployeeId(mission.getAssignedEmployeeId())
                        .recipientEmail(employee.getEmail())
                        .recipientFirstName(
                                employee.getFirstName() == null || employee.getFirstName().isBlank()
                                        ? "colleague" : employee.getFirstName())
                        .build();

                missionEventPublisher.publishMissionAssigned(event);
                LOGGER.info("Événement MQ publié pour la mission " + mission.getId());
                return;
            }

            Map<String, Object> variables = new HashMap<>();
            variables.put("firstName",
                    employee.getFirstName() == null || employee.getFirstName().isBlank()
                            ? "colleague" : employee.getFirstName());
            variables.put("missionTitle", mission.getTitle() == null ? "" : mission.getTitle());
            variables.put("missionDescription",
                    mission.getDescription() == null ? "" : mission.getDescription());
            variables.put("startDate",
                    mission.getStartDate() == null ? "" : mission.getStartDate().format(DATE_FORMAT));
            variables.put("endDate",
                    mission.getEndDate() == null ? "" : mission.getEndDate().format(DATE_FORMAT));

            MailRequest request = MailRequest.builder()
                    .to(employee.getEmail())
                    .subject("New mission assigned: " + mission.getTitle())
                    .template("mission-assigned")
                    .locale("en")
                    .variables(variables)
                    .build();

            notificationClient.sendNotification(request);
            LOGGER.info("Notification de mission envoyée à l'employé pour la mission " + mission.getId());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "La mission " + mission.getId()
                            + " est enregistrée, mais l'envoi de la notification a échoué.",
                    e);
        }
    }

    public MissionDto updateMission(Long id, MissionRequest request) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Mission not found with id: " + id)
                );

        mission.setTitle(request.getTitle());
        mission.setDescription(request.getDescription());
        mission.setStartDate(request.getStartDate());
        mission.setEndDate(request.getEndDate());
        mission.setAssignedEmployeeId(request.getAssignedEmployeeId());
        mission.setLocation(request.getLocation());
        mission.setStatus(request.getStatus());
        mission.setPriority(request.getPriority());
        mission.setBudget(request.getBudget());

        Mission updatedMission = missionRepository.save(mission);

        return missionDtoConverter.convert(updatedMission);
    }

    public void deleteMission(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Mission not found with id: " + id)
                );

        missionRepository.delete(mission);
    }

    public EmployeeDto getAssignedEmployee(Long employeeId) {
        return hrClient.getEmployeeById(employeeId);
    }
}
