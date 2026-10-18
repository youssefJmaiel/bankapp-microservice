package com.bankapp.partner.controller;

import com.bankapp.partner.config.SecurityConfig;
import com.bankapp.partner.dto.PartnerDirection;
import com.bankapp.partner.dto.PartnerDto;
import com.bankapp.partner.dto.PartnerRequest;
import com.bankapp.partner.dto.PartnerType;
import com.bankapp.partner.dto.ProcessedFlowType;
import com.bankapp.partner.service.PartnerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PartnerController.class)
@Import(SecurityConfig.class)
class PartnerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PartnerService partnerService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private PartnerDto partnerDto() {
        return PartnerDto.builder()
                .id(1L)
                .alias("bank-adapter")
                .type(PartnerType.MESSAGE)
                .direction(PartnerDirection.OUTBOUND)
                .application("bankapp")
                .processedFlowType(ProcessedFlowType.MESSAGE)
                .description("Bank messaging partner")
                .build();
    }

    private String validRequestJson() {
        return """
                {
                  "alias": "bank-adapter",
                  "type": "MESSAGE",
                  "direction": "OUTBOUND",
                  "application": "bankapp",
                  "processedFlowType": "MESSAGE",
                  "description": "Bank messaging partner"
                }
                """;
    }

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/partners"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(partnerService);
    }

    @Test
    void shouldAllowUserToReadPartners() throws Exception {
        when(partnerService.getAllPartners()).thenReturn(List.of(partnerDto()));

        mockMvc.perform(get("/api/partners")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].alias").value("bank-adapter"));

        verify(partnerService).getAllPartners();
    }

    @Test
    void shouldAllowAdminToCreatePartner() throws Exception {
        when(partnerService.savePartner(any(PartnerRequest.class)))
                .thenReturn(partnerDto());

        mockMvc.perform(post("/api/partners")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType("application/json")
                        .content(validRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.alias").value("bank-adapter"));

        verify(partnerService).savePartner(any(PartnerRequest.class));
    }

    @Test
    void shouldForbidUserFromCreatingPartner() throws Exception {
        mockMvc.perform(post("/api/partners")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType("application/json")
                        .content(validRequestJson()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(partnerService);
    }

    @Test
    void shouldAllowAdminToDeletePartner() throws Exception {
        doNothing().when(partnerService).deletePartner(1L);

        mockMvc.perform(delete("/api/partners/1")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());

        verify(partnerService).deletePartner(1L);
    }

    @Test
    void shouldRejectInvalidPartnerRequest() throws Exception {
        mockMvc.perform(post("/api/partners")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType("application/json")
                        .content("""
                                {
                                  "alias": " ",
                                  "type": null,
                                  "direction": "OUTBOUND",
                                  "processedFlowType": null,
                                  "description": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(partnerService);
    }
}
