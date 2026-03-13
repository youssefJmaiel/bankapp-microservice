package com.bankapp.messagerouter.controller;

import com.bankapp.messagerouter.dto.MessageRequest;
import com.bankapp.messagerouter.entity.Message;
import com.bankapp.messagerouter.error.MessageNotFoundException;
import com.bankapp.messagerouter.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------------------- GET ALL ----------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllMessages_shouldReturnList() throws Exception {
        Message message = new Message();
        message.setContent("Hello World");

        when(messageService.getAllMessages())
                .thenReturn(Collections.singletonList(message));

        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Hello World"));
    }

    // ---------------------- GET PAGINATED ----------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void getMessagesPaged_shouldReturnPage() throws Exception {
        Message message = new Message();
        message.setContent("Paged Message");

        Page<Message> page = new PageImpl<>(Collections.singletonList(message));
        when(messageService.getMessagesPaginated(0, 10, "timestamp", "desc"))
                .thenReturn(page);

        mockMvc.perform(get("/api/messages/paged")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "timestamp")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").value("Paged Message"));
    }

    // ---------------------- GET BY ID ----------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void getMessageById_shouldReturnMessage_whenFound() throws Exception {
        Message message = new Message();
        message.setId(1L);
        message.setContent("Test Message");
        message.setTimestamp(LocalDateTime.now());

        when(messageService.getMessageById(1L)).thenReturn(message);

        mockMvc.perform(get("/api/message/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Test Message"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getMessageById_shouldReturnNotFound_whenMissing() throws Exception {
        doThrow(new MessageNotFoundException("Message with ID 999 not found"))
                .when(messageService).getMessageById(999L);

        mockMvc.perform(get("/api/message/999"))
                .andExpect(status().isNotFound());
    }

    // ---------------------- CREATE ----------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void createMessage_shouldReturnCreated() throws Exception {
        Message message = new Message();
        message.setContent("New Message");

        when(messageService.saveMessage(any(Message.class))).thenReturn(message);

        mockMvc.perform(post("/api/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("New Message"));
    }

    // ---------------------- DELETE ----------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteMessage_shouldReturnNoContent_whenExists() throws Exception {
        doNothing().when(messageService).deleteMessage(1L);

        mockMvc.perform(delete("/api/message/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteMessage_shouldReturnNotFound_whenMissing() throws Exception {
        doThrow(new MessageNotFoundException("Message with ID 999 not found"))
                .when(messageService).deleteMessage(999L);

        mockMvc.perform(delete("/api/message/999"))
                .andExpect(status().isNotFound());
    }

    // ---------------------- SEND MESSAGE ----------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void sendMessage_shouldReturnCreated() throws Exception {
        MessageRequest request = new MessageRequest();
        request.setContent("Hello");
        request.setSender("Alice");
        request.setReceiver("Bob");

        Message message = new Message();
        message.setContent("Hello");
        message.setSender("Alice");
        message.setReceiver("Bob");

        when(messageService.sendMessage("Hello", "Alice", "Bob")).thenReturn(message);

        mockMvc.perform(post("/api/message/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Hello"))
                .andExpect(jsonPath("$.sender").value("Alice"))
                .andExpect(jsonPath("$.receiver").value("Bob"));
    }
}