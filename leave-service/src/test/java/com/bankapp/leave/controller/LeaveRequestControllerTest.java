package com.bankapp.leave.controller;

import com.bankapp.leave.domain.CreateLeaveRequest;
import com.bankapp.leave.domain.LeaveDecisionRequest;
import com.bankapp.leave.domain.LeaveRequestDto;
import com.bankapp.leave.exception.LeaveExceptionHandler;
import com.bankapp.leave.service.LeaveRequestService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LeaveRequestController.class)
@Import({
    LeaveExceptionHandler.class,
    LeaveRequestControllerTest.TestSecurityConfig.class
})
class LeaveRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LeaveRequestService leaveRequestService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private Jwt jwtFor(String subject, String username) {
        Jwt.Builder builder = Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .subject(subject)
            .issuedAt(Instant.parse("2026-01-01T00:00:00Z"))
            .expiresAt(Instant.parse("2027-01-01T00:00:00Z"));

        if (username != null) {
            builder.claim("preferred_username", username);
        }

        return builder.build();
    }

    private LeaveRequestDto leaveDto(Long id, String username, String status) {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setId(id);
        dto.setRequesterUsername(username);
        dto.setStartDate(LocalDate.now().plusDays(5));
        dto.setEndDate(LocalDate.now().plusDays(7));
        dto.setReason("Congé annuel");
        dto.setStatus(status);
        dto.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        dto.setUpdatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        return dto;
    }

    @Test
    void createRequest_validRequest_returnsCreatedAndUsesPreferredUsername()
            throws Exception {
        CreateLeaveRequest request = new CreateLeaveRequest();
        request.setStartDate(LocalDate.now().plusDays(5));
        request.setEndDate(LocalDate.now().plusDays(7));
        request.setReason("Congé annuel");

        when(leaveRequestService.createRequest(
                eq("youssef"), any(CreateLeaveRequest.class)))
            .thenReturn(leaveDto(1L, "youssef", "PENDING"));

        mockMvc.perform(post("/api/leaves")
                .with(jwt().jwt(jwtFor("user-123", "youssef"))
                    .authorities(() -> "ROLE_USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.requesterUsername").value("youssef"))
            .andExpect(jsonPath("$.status").value("PENDING"));

        verify(leaveRequestService).createRequest(
            eq("youssef"), any(CreateLeaveRequest.class));
    }

    @Test
    void createRequest_missingReason_returnsBadRequest() throws Exception {
        String body = "{"
            + "\"startDate\":\"" + LocalDate.now().plusDays(5) + "\","
            + "\"endDate\":\"" + LocalDate.now().plusDays(7) + "\","
            + "\"reason\":\" \""
            + "}";

        mockMvc.perform(post("/api/leaves")
                .with(jwt().jwt(jwtFor("user-123", "youssef"))
                    .authorities(() -> "ROLE_USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());

        verify(leaveRequestService, never()).createRequest(
            any(String.class), any(CreateLeaveRequest.class));
    }

    @Test
    void getMyRequests_returnsRequestsForJwtUsername() throws Exception {
        when(leaveRequestService.getMyRequests("youssef"))
            .thenReturn(Collections.singletonList(
                leaveDto(1L, "youssef", "PENDING")));

        mockMvc.perform(get("/api/leaves/my")
                .with(jwt().jwt(jwtFor("user-123", "youssef"))
                    .authorities(() -> "ROLE_USER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].requesterUsername").value("youssef"));

        verify(leaveRequestService).getMyRequests("youssef");
    }

    @Test
    void cancelMyRequest_returnsCancelledRequest() throws Exception {
        when(leaveRequestService.cancelMyRequest(1L, "youssef"))
            .thenReturn(leaveDto(1L, "youssef", "CANCELLED"));

        mockMvc.perform(delete("/api/leaves/1")
                .with(jwt().jwt(jwtFor("user-123", "youssef"))
                    .authorities(() -> "ROLE_USER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(leaveRequestService).cancelMyRequest(1L, "youssef");
    }

    @Test
    void getAllRequests_adminReturnsAllRequests() throws Exception {
        when(leaveRequestService.getAllRequests())
            .thenReturn(List.of(leaveDto(1L, "youssef", "PENDING")));

        mockMvc.perform(get("/api/leaves/all")
                .with(jwt().jwt(jwtFor("admin-123", "admin"))
                    .authorities(() -> "ROLE_ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1));

        verify(leaveRequestService).getAllRequests();
    }

    @Test
    void getAllRequests_userIsForbidden() throws Exception {
        mockMvc.perform(get("/api/leaves/all")
                .with(jwt().jwt(jwtFor("user-123", "youssef"))
                    .authorities(() -> "ROLE_USER")))
            .andExpect(status().isForbidden());

        verify(leaveRequestService, never()).getAllRequests();
    }

    @Test
    void decideRequest_adminCanApproveRequest() throws Exception {
        LeaveDecisionRequest request = new LeaveDecisionRequest();
        request.setDecision(LeaveDecisionRequest.Decision.APPROVED);

        when(leaveRequestService.decideRequest(
                eq(1L), eq("admin"), any(LeaveDecisionRequest.class)))
            .thenReturn(leaveDto(1L, "youssef", "APPROVED"));

        mockMvc.perform(patch("/api/leaves/1/decision")
                .with(jwt().jwt(jwtFor("admin-123", "admin"))
                    .authorities(() -> "ROLE_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(leaveRequestService).decideRequest(
            eq(1L), eq("admin"), any(LeaveDecisionRequest.class));
    }

    @Test
    void decideRequest_userIsForbidden() throws Exception {
        String body = "{\"decision\":\"APPROVED\"}";

        mockMvc.perform(patch("/api/leaves/1/decision")
                .with(jwt().jwt(jwtFor("user-123", "youssef"))
                    .authorities(() -> "ROLE_USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isForbidden());

        verify(leaveRequestService, never()).decideRequest(
            any(Long.class), any(String.class), any(LeaveDecisionRequest.class));
    }

    @Test
    void decideRequest_missingDecision_returnsBadRequest() throws Exception {
        mockMvc.perform(patch("/api/leaves/1/decision")
                .with(jwt().jwt(jwtFor("admin-123", "admin"))
                    .authorities(() -> "ROLE_ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());

        verify(leaveRequestService, never()).decideRequest(
            any(Long.class), any(String.class), any(LeaveDecisionRequest.class));
    }

    @Test
    void createRequest_usesSubjectWhenPreferredUsernameIsMissing()
            throws Exception {
        CreateLeaveRequest request = new CreateLeaveRequest();
        request.setStartDate(LocalDate.now().plusDays(5));
        request.setEndDate(LocalDate.now().plusDays(6));
        request.setReason("Rendez-vous");

        when(leaveRequestService.createRequest(
                eq("subject-456"), any(CreateLeaveRequest.class)))
            .thenReturn(leaveDto(2L, "subject-456", "PENDING"));

        mockMvc.perform(post("/api/leaves")
                .with(jwt().jwt(jwtFor("subject-456", null))
                    .authorities(() -> "ROLE_USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.requesterUsername").value("subject-456"));

        verify(leaveRequestService).createRequest(
            eq("subject-456"), any(CreateLeaveRequest.class));
    }

    @TestConfiguration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain leaveControllerTestSecurityFilterChain(
                HttpSecurity http) throws Exception {
            http.csrf().disable()
                .authorizeRequests()
                    .antMatchers("/api/**").authenticated()
                    .anyRequest().permitAll()
                .and()
                .oauth2ResourceServer()
                    .jwt();

            return http.build();
        }
    }
}
