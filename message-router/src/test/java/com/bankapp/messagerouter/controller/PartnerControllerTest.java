package com.bankapp.messagerouter.controller;

import com.bankapp.messagerouter.entity.Partner;
import com.bankapp.messagerouter.entity.PartnerDirection;
import com.bankapp.messagerouter.entity.PartnerType;
import com.bankapp.messagerouter.entity.ProcessedFlowType;
import com.bankapp.messagerouter.error.PartnerNotFoundException;
import com.bankapp.messagerouter.service.PartnerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PartnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PartnerService partnerService;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------------------- GET ALL ----------------------
    @Test
    @WithMockUser(roles = "USER")
    void getAllPartners_shouldReturnList() throws Exception {
        Partner partner = new Partner();
        partner.setAlias("Test Partner");

        when(partnerService.getAllPartners())
                .thenReturn(Collections.singletonList(partner));

        mockMvc.perform(get("/api/partners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].alias").value("Test Partner"));
    }

    // ---------------------- GET BY ID ----------------------
    @Test
    @WithMockUser(roles = "USER")
    void getPartnerById_shouldReturnPartner_whenFound() throws Exception {
        Partner partner = new Partner();
        partner.setId(1L);
        partner.setAlias("Partner 1");

        when(partnerService.getPartnerById(1L)).thenReturn(partner);

        mockMvc.perform(get("/api/partners/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alias").value("Partner 1"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getPartnerById_shouldReturnNotFound_whenMissing() throws Exception {
        doThrow(new PartnerNotFoundException("Partner with ID 999 not found"))
                .when(partnerService).getPartnerById(999L);

        mockMvc.perform(get("/api/partners/999"))
                .andExpect(status().isNotFound());
    }

    // ---------------------- CREATE ----------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void createPartner_shouldReturnCreated() throws Exception {
        Partner partner = new Partner();
        partner.setAlias("New Partner");
        partner.setType(PartnerType.MESSAGE);
        partner.setDirection(PartnerDirection.INBOUND);
        partner.setProcessedFlowType(ProcessedFlowType.MESSAGE);
        partner.setDescription("Valid Description");

        when(partnerService.savePartner(any(Partner.class))).thenReturn(partner);

        mockMvc.perform(post("/api/partners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partner)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alias").value("New Partner"))
                .andExpect(jsonPath("$.type").value("MESSAGE"))
                .andExpect(jsonPath("$.direction").value("INBOUND"))
                .andExpect(jsonPath("$.processedFlowType").value("MESSAGE"))
                .andExpect(jsonPath("$.description").value("Valid Description"));
    }

    // ---------------------- DELETE ----------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePartner_shouldReturnNoContent_whenExists() throws Exception {
        doNothing().when(partnerService).deletePartner(1L);

        mockMvc.perform(delete("/api/partners/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletePartner_shouldReturnNotFound_whenMissing() throws Exception {
        doThrow(new PartnerNotFoundException("Partner with ID 999 not found"))
                .when(partnerService).deletePartner(999L);

        mockMvc.perform(delete("/api/partners/999"))
                .andExpect(status().isNotFound());
    }
}