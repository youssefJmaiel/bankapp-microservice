package com.bankapp.mission.controller;

import com.bankapp.hr.dto.EmployeeDto;
import com.bankapp.mission.dto.MissionDto;
import com.bankapp.mission.dto.MissionRequest;
import com.bankapp.mission.service.MissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MissionService missionService;

    private MissionDto missionDto(Long id) {
        return MissionDto.builder()
                .id(id)
                .title("Migration bancaire")
                .description("Migration des données")
                .startDate(LocalDateTime.of(2026, 11, 1, 9, 0))
                .endDate(LocalDateTime.of(2026, 11, 2, 17, 0))
                .assignedEmployeeId(10L)
                .assignedEmployeeName("Test Employee")
                .location("Tunis")
                .status("PENDING")
                .priority("MEDIUM")
                .budget(new BigDecimal("1500.00"))
                .build();
    }

    private MissionRequest validRequest() {
        return MissionRequest.builder()
                .title("Migration bancaire")
                .description("Migration des données")
                .startDate(LocalDateTime.of(2026, 11, 1, 9, 0))
                .endDate(LocalDateTime.of(2026, 11, 2, 17, 0))
                .assignedEmployeeId(10L)
                .location("Tunis")
                .status("PENDING")
                .priority("MEDIUM")
                .budget(new BigDecimal("1500.00"))
                .build();
    }

    @Test
    void anonymousUserCannotReadMissions() throws Exception {
        mockMvc.perform(get("/api/missions"))
                .andExpect(status().isUnauthorized());

        verify(missionService, never()).getAllMissions();
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCanReadMissions() throws Exception {
        when(missionService.getAllMissions())
                .thenReturn(List.of(missionDto(1L)));

        mockMvc.perform(get("/api/missions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Migration bancaire"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCanReadMissionById() throws Exception {
        when(missionService.getMissionById(1L)).thenReturn(missionDto(1L));

        mockMvc.perform(get("/api/missions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Migration bancaire"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateMission() throws Exception {
        when(missionService.createMission(any(MissionRequest.class)))
                .thenReturn(missionDto(1L));

        mockMvc.perform(post("/api/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(missionService).createMission(any(MissionRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotCreateMission() throws Exception {
        mockMvc.perform(post("/api/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());

        verify(missionService, never()).createMission(any(MissionRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanUpdateMission() throws Exception {
        when(missionService.updateMission(eq(1L), any(MissionRequest.class)))
                .thenReturn(missionDto(1L));

        mockMvc.perform(put("/api/missions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(missionService).updateMission(eq(1L), any(MissionRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotUpdateMission() throws Exception {
        mockMvc.perform(put("/api/missions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());

        verify(missionService, never())
                .updateMission(eq(1L), any(MissionRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanDeleteMission() throws Exception {
        doNothing().when(missionService).deleteMission(1L);

        mockMvc.perform(delete("/api/missions/1"))
                .andExpect(status().isNoContent());

        verify(missionService).deleteMission(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotDeleteMission() throws Exception {
        mockMvc.perform(delete("/api/missions/1"))
                .andExpect(status().isForbidden());

        verify(missionService, never()).deleteMission(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void invalidMissionRequestReturnsBadRequestForAdmin() throws Exception {
        String invalidJson = """
                {
                  "title": " ",
                  "startDate": "2026-11-01T09:00:00",
                  "endDate": "2026-11-02T17:00:00",
                  "assignedEmployeeId": 10
                }
                """;

        mockMvc.perform(post("/api/missions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(missionService, never()).createMission(any(MissionRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCanReadAssignedEmployee() throws Exception {
        EmployeeDto employee = EmployeeDto.builder()
                .id(10L)
                .firstName("Test")
                .lastName("Employee")
                .email("test@example.com")
                .build();

        when(missionService.getMissionById(1L)).thenReturn(missionDto(1L));
        when(missionService.getAssignedEmployee(10L)).thenReturn(employee);

        mockMvc.perform(get("/api/missions/1/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.firstName").value("Test"));

        verify(missionService).getAssignedEmployee(10L);
    }
}
