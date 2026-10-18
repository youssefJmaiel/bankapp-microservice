package com.bankapp.hr.controller;

import com.bankapp.hr.dto.DepartmentDto;
import com.bankapp.hr.service.DepartmentService;
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

@WebMvcTest(DepartmentController.class)
@Import(SecurityConfig.class)
class DepartmentControllerTest {
    @Autowired MockMvc mvc;
    @MockBean DepartmentService service;

    private DepartmentDto department(Long id, String name) {
        return DepartmentDto.builder().id(id).name(name).build();
    }

    @Test
    void listAsUserReturnsOk() throws Exception {
        when(service.getAllDepartments()).thenReturn(List.of(department(1L, "IT")));
        mvc.perform(get("/api/departments")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("IT"));
    }

    @Test
    void getByIdReturnsDepartment() throws Exception {
        when(service.getDepartmentById(2L)).thenReturn(department(2L, "Finance"));
        mvc.perform(get("/api/departments/2")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Finance"));
    }

    @Test
    void adminCanCreateDepartment() throws Exception {
        when(service.createDepartment(any())).thenReturn(department(3L, "IT"));
        mvc.perform(post("/api/departments")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"IT\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(3));
        verify(service).createDepartment(any());
    }

    @Test
    void blankDepartmentNameReturnsBadRequest() throws Exception {
        mvc.perform(post("/api/departments")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\" \"}"))
            .andExpect(status().isBadRequest());
        verify(service, never()).createDepartment(any());
    }

    @Test
    void userCannotCreateDepartment() throws Exception {
        mvc.perform(post("/api/departments")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"IT\"}"))
            .andExpect(status().isForbidden());
        verify(service, never()).createDepartment(any());
    }

    @Test
    void adminDeleteReturnsNoContent() throws Exception {
        mvc.perform(delete("/api/departments/8")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isNoContent());
        verify(service).deleteDepartment(8L);
    }

    @Test
    void userCannotDeleteDepartment() throws Exception {
        mvc.perform(delete("/api/departments/8")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
            .andExpect(status().isForbidden());
        verify(service, never()).deleteDepartment(8L);
    }

    @Test
    void anonymousCannotListDepartments() throws Exception {
        mvc.perform(get("/api/departments")).andExpect(status().isUnauthorized());
        verify(service, never()).getAllDepartments();
    }

    @Test
    void adminCanUpdateDepartment() throws Exception {
        when(service.updateDepartment(eq(3L), any()))
            .thenReturn(department(3L, "Operations"));
        mvc.perform(put("/api/departments/3")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Operations\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Operations"));
    }
}
