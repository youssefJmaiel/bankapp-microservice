package com.bankapp.hr.controller;

import com.bankapp.hr.dto.EmployeeDto;
import com.bankapp.hr.dto.KeycloakUserDto;
import com.bankapp.hr.service.EmployeeService;
import com.bankapp.hr.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@Import(SecurityConfig.class)
class EmployeeControllerTest {
    @Autowired MockMvc mvc;
    @MockBean EmployeeService service;

    private EmployeeDto employee(Long id) {
        return EmployeeDto.builder().id(id).firstName("Amira")
            .lastName("Ben Ali").email("amira@example.com")
            .status("ACTIVE").build();
    }

    @Test
    void listAsUserReturnsOk() throws Exception {
        when(service.getAllEmployees()).thenReturn(List.of(employee(1L)));
        mvc.perform(get("/api/employees")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1));
        verify(service).getAllEmployees();
    }

    @Test
    void getByIdAsUserReturnsEmployee() throws Exception {
        when(service.getEmployeeById(1L)).thenReturn(employee(1L));
        mvc.perform(get("/api/employees/1")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("amira@example.com"));
    }

    @Test
    void createAsAdminReturnsEmployee() throws Exception {
        when(service.createEmployee(any())).thenReturn(employee(2L));
        mvc.perform(post("/api/employees")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Amira\",\"lastName\":\"Ben Ali\",\"email\":\"amira@example.com\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(2));
        verify(service).createEmployee(any());
    }

    @Test
    void createWithInvalidEmailReturnsBadRequest() throws Exception {
        mvc.perform(post("/api/employees")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Amira\",\"lastName\":\"Ben Ali\",\"email\":\"invalid\"}"))
            .andExpect(status().isBadRequest());
        verify(service, never()).createEmployee(any());
    }

    @Test
    void createWithBlankFirstNameReturnsBadRequest() throws Exception {
        mvc.perform(post("/api/employees")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\" \",\"lastName\":\"Ben Ali\",\"email\":\"amira@example.com\"}"))
            .andExpect(status().isBadRequest());
        verify(service, never()).createEmployee(any());
    }

    @Test
    void userCannotCreateEmployee() throws Exception {
        mvc.perform(post("/api/employees")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Amira\",\"lastName\":\"Ben Ali\",\"email\":\"amira@example.com\"}"))
            .andExpect(status().isForbidden());
        verify(service, never()).createEmployee(any());
    }

    @Test
    void getKeycloakUsersAsAdminReturnsUsers() throws Exception {
        KeycloakUserDto user = KeycloakUserDto.builder()
            .id("kc-user-123")
            .username("testuser")
            .email("testuser@example.com")
            .firstName("Test")
            .lastName("User")
            .enabled(true)
            .build();

        when(service.getKeycloakUsers()).thenReturn(List.of(user));

        mvc.perform(get("/api/employees/keycloak-users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("kc-user-123"))
            .andExpect(jsonPath("$[0].username").value("testuser"))
            .andExpect(jsonPath("$[0].email").value("testuser@example.com"))
            .andExpect(jsonPath("$[0].enabled").value(true));

        verify(service).getKeycloakUsers();
    }

    @Test
    void userCannotListKeycloakUsers() throws Exception {
        mvc.perform(get("/api/employees/keycloak-users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
            .andExpect(status().isForbidden());

        verify(service, never()).getKeycloakUsers();
    }

    @Test
    void anonymousCannotListKeycloakUsers() throws Exception {
        mvc.perform(get("/api/employees/keycloak-users"))
            .andExpect(status().isUnauthorized());

        verify(service, never()).getKeycloakUsers();
    }

    @Test
    void anonymousCannotListEmployees() throws Exception {
        mvc.perform(get("/api/employees")).andExpect(status().isUnauthorized());
        verify(service, never()).getAllEmployees();
    }

    @Test
    void userCannotListArchivedEmployees() throws Exception {
        mvc.perform(get("/api/employees/archived")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
            .andExpect(status().isForbidden());
        verify(service, never()).getArchivedEmployees();
    }

    @Test
    void adminDeleteReturnsNoContent() throws Exception {
        mvc.perform(delete("/api/employees/5")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isNoContent());
        verify(service).deleteEmployee(5L);
    }

    @Test
    void adminUpdateReturnsEmployee() throws Exception {
        when(service.updateEmployee(eq(3L), any())).thenReturn(employee(3L));
        mvc.perform(put("/api/employees/3")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Amira\",\"lastName\":\"Ben Ali\",\"email\":\"amira@example.com\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void adminCanRestoreEmployee() throws Exception {
        when(service.restoreEmployee(4L)).thenReturn(employee(4L));
        mvc.perform(post("/api/employees/4/restore")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(4));
    }
}
