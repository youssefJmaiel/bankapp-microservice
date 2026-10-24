package com.bankapp.mission.service;

import com.bankapp.hr.dto.EmployeeDto;
import com.bankapp.mission.client.HrClient;
import com.bankapp.mission.client.NotificationClient;
import com.bankapp.mission.converter.MissionDtoConverter;
import com.bankapp.mission.converter.MissionRequestConverter;
import com.bankapp.mission.dto.MissionDto;
import com.bankapp.mission.dto.MissionRequest;
import com.bankapp.mission.entity.Mission;
import com.bankapp.mission.messaging.MissionEventPublisher;
import com.bankapp.mission.repository.MissionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.AfterEach;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.test.util.ReflectionTestUtils;
import com.bankapp.notification.dto.MailRequest;
import com.bankapp.mission.dto.MissionAssignedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MissionServiceTest {

    @Mock
    private MissionRepository missionRepository;

    @Mock
    private HrClient hrClient;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private MissionEventPublisher missionEventPublisher;

    private MissionService missionService;

    @BeforeEach
    void setUp() {
        missionService = new MissionService(
                missionRepository,
                hrClient,
                notificationClient,
                missionEventPublisher,
                new MissionRequestConverter(),
                new MissionDtoConverter()
        );
    }

    private Mission mission(Long id) {
        Mission mission = new Mission();
        mission.setId(id);
        mission.setTitle("Migration bancaire");
        mission.setDescription("Migration technique");
        mission.setStartDate(LocalDateTime.of(2026, 11, 1, 9, 0));
        mission.setEndDate(LocalDateTime.of(2026, 11, 5, 17, 0));
        mission.setAssignedEmployeeId(42L);
        mission.setLocation("Tunis");
        mission.setStatus("PENDING");
        mission.setPriority("MEDIUM");
        mission.setBudget(new BigDecimal("1500.00"));
        return mission;
    }

    private MissionRequest request() {
        return MissionRequest.builder()
                .title("Migration bancaire")
                .description("Migration technique")
                .startDate(LocalDateTime.of(2026, 11, 1, 9, 0))
                .endDate(LocalDateTime.of(2026, 11, 5, 17, 0))
                .assignedEmployeeId(42L)
                .location("Tunis")
                .budget(new BigDecimal("1500.00"))
                .build();
    }

    @Test
    void getAllMissionsReturnsConvertedMissions() {
        when(missionRepository.findAll()).thenReturn(List.of(mission(1L)));

        List<MissionDto> result = missionService.getAllMissions();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Migration bancaire", result.get(0).getTitle());
        assertEquals(42L, result.get(0).getAssignedEmployeeId());
        verify(missionRepository).findAll();
    }

    @Test
    void getMissionByIdReturnsMissionWhenItExists() {
        when(missionRepository.findById(1L))
                .thenReturn(Optional.of(mission(1L)));

        MissionDto result = missionService.getMissionById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Migration bancaire", result.getTitle());
        verify(missionRepository).findById(1L);
    }

    @Test
    void getMissionByIdThrowsWhenMissionDoesNotExist() {
        when(missionRepository.findById(999L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> missionService.getMissionById(999L)
        );

        assertEquals("Mission not found with id: 999", exception.getMessage());
    }

    @Test
    void createMissionSavesMissionAndAppliesDefaultValues() {
        when(missionRepository.save(any(Mission.class)))
                .thenAnswer(invocation -> {
                    Mission saved = invocation.getArgument(0);
                    saved.setId(10L);
                    return saved;
                });

        MissionDto result = missionService.createMission(request());

        assertEquals(10L, result.getId());
        assertEquals("Migration bancaire", result.getTitle());
        assertEquals("PENDING", result.getStatus());
        assertEquals("MEDIUM", result.getPriority());

        verify(missionRepository).save(argThat(saved ->
                "Migration bancaire".equals(saved.getTitle())
                && "PENDING".equals(saved.getStatus())
                && "MEDIUM".equals(saved.getPriority())
                && Long.valueOf(42L).equals(saved.getAssignedEmployeeId())
        ));
    }

    @Test
    void updateMissionChangesAndSavesExistingMission() {
        Mission existing = mission(1L);
        MissionRequest update = request();
        update.setTitle("Nouvelle mission");
        update.setStatus("IN_PROGRESS");
        update.setPriority("HIGH");

        when(missionRepository.findById(1L))
                .thenReturn(Optional.of(existing));
        when(missionRepository.save(any(Mission.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MissionDto result = missionService.updateMission(1L, update);

        assertEquals("Nouvelle mission", result.getTitle());
        assertEquals("IN_PROGRESS", result.getStatus());
        assertEquals("HIGH", result.getPriority());
        verify(missionRepository).save(existing);
    }

    @Test
    void updateMissionThrowsWhenMissionDoesNotExist() {
        when(missionRepository.findById(999L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> missionService.updateMission(999L, request())
        );

        assertEquals("Mission not found with id: 999", exception.getMessage());
        verify(missionRepository, never()).save(any(Mission.class));
    }

    @Test
    void deleteMissionDeletesExistingMission() {
        Mission existing = mission(1L);
        when(missionRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        missionService.deleteMission(1L);

        verify(missionRepository).delete(existing);
    }

    @Test
    void getAssignedEmployeeDelegatesToHrClient() {
        EmployeeDto employee = new EmployeeDto();
        when(hrClient.getEmployeeById(42L)).thenReturn(employee);

        EmployeeDto result = missionService.getAssignedEmployee(42L);

        assertSame(employee, result);
        verify(hrClient).getEmployeeById(42L);
    }


    @AfterEach
    void cleanTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    private void beginTransactionSynchronization() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
    }

    private void executeAfterCommitCallbacks() {
        for (TransactionSynchronization synchronization :
                TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }
    }

    @Test
    void mqNotificationIsPublishedOnlyAfterCommit() {
        ReflectionTestUtils.setField(missionService, "mqNotificationsEnabled", true);

        EmployeeDto employee = EmployeeDto.builder()
                .id(42L)
                .firstName("Youssef")
                .email("employee@example.com")
                .build();
        when(hrClient.getEmployeeById(42L)).thenReturn(employee);
        when(missionRepository.save(any(Mission.class))).thenAnswer(invocation -> {
            Mission saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        beginTransactionSynchronization();
        MissionDto result = missionService.createMission(request());

        assertEquals(10L, result.getId());
        verifyNoInteractions(missionEventPublisher, notificationClient);

        executeAfterCommitCallbacks();

        ArgumentCaptor<MissionAssignedEvent> captor =
                ArgumentCaptor.forClass(MissionAssignedEvent.class);
        verify(missionEventPublisher).publishMissionAssigned(captor.capture());
        MissionAssignedEvent event = captor.getValue();

        assertEquals(10L, event.getMissionId());
        assertEquals("Migration bancaire", event.getMissionTitle());
        assertEquals(42L, event.getAssignedEmployeeId());
        assertEquals("employee@example.com", event.getRecipientEmail());
        assertEquals("Youssef", event.getRecipientFirstName());
        assertNotNull(event.getEventId());
        verifyNoInteractions(notificationClient);
    }

    @Test
    void directNotificationIsSentAfterCommitWhenMqIsDisabled() {
        ReflectionTestUtils.setField(missionService, "mqNotificationsEnabled", false);

        EmployeeDto employee = EmployeeDto.builder()
                .id(42L)
                .firstName("Youssef")
                .email("employee@example.com")
                .build();
        when(hrClient.getEmployeeById(42L)).thenReturn(employee);
        when(missionRepository.save(any(Mission.class))).thenAnswer(invocation -> {
            Mission saved = invocation.getArgument(0);
            saved.setId(11L);
            return saved;
        });

        beginTransactionSynchronization();
        missionService.createMission(request());

        verifyNoInteractions(missionEventPublisher, notificationClient);
        executeAfterCommitCallbacks();

        ArgumentCaptor<MailRequest> captor =
                ArgumentCaptor.forClass(MailRequest.class);
        verify(notificationClient).sendNotification(captor.capture());

        MailRequest mail = captor.getValue();
        assertEquals("employee@example.com", mail.getTo());
        assertEquals("New mission assigned: Migration bancaire", mail.getSubject());
        assertEquals("mission-assigned", mail.getTemplate());
        assertEquals("Youssef", mail.getVariables().get("firstName"));
        verifyNoInteractions(missionEventPublisher);
    }

    @Test
    void missingEmployeeEmailDoesNotSendNotification() {
        ReflectionTestUtils.setField(missionService, "mqNotificationsEnabled", true);

        EmployeeDto employee = EmployeeDto.builder()
                .id(42L)
                .firstName("Youssef")
                .email("   ")
                .build();
        when(hrClient.getEmployeeById(42L)).thenReturn(employee);
        when(missionRepository.save(any(Mission.class))).thenAnswer(invocation -> {
            Mission saved = invocation.getArgument(0);
            saved.setId(12L);
            return saved;
        });

        beginTransactionSynchronization();
        MissionDto result = missionService.createMission(request());

        assertEquals(12L, result.getId());
        executeAfterCommitCallbacks();

        verify(missionRepository).save(any(Mission.class));
        verifyNoInteractions(missionEventPublisher, notificationClient);
    }

    @Test
    void publisherFailureDoesNotUndoSavedMission() {
        ReflectionTestUtils.setField(missionService, "mqNotificationsEnabled", true);

        EmployeeDto employee = EmployeeDto.builder()
                .id(42L)
                .firstName("Youssef")
                .email("employee@example.com")
                .build();
        when(hrClient.getEmployeeById(42L)).thenReturn(employee);
        when(missionRepository.save(any(Mission.class))).thenAnswer(invocation -> {
            Mission saved = invocation.getArgument(0);
            saved.setId(13L);
            return saved;
        });
        doThrow(new IllegalStateException("MQ indisponible"))
                .when(missionEventPublisher)
                .publishMissionAssigned(any(MissionAssignedEvent.class));

        beginTransactionSynchronization();
        MissionDto result = missionService.createMission(request());

        assertEquals(13L, result.getId());
        executeAfterCommitCallbacks();

        verify(missionRepository).save(any(Mission.class));
        verify(missionEventPublisher).publishMissionAssigned(
                any(MissionAssignedEvent.class));
        verifyNoInteractions(notificationClient);
    }

}
