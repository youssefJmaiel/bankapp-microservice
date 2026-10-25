package com.bankapp.messagerouter.controller;

import com.bankapp.message.dto.MessageDto;
import com.bankapp.message.dto.MessageRequest;
import com.bankapp.messagerouter.error.MessageNotFoundException;
import com.bankapp.messagerouter.client.RecipientDirectoryClient;
import com.bankapp.messagerouter.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@AutoConfigureMockMvc
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    private RecipientDirectoryClient recipientDirectoryClient;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------------------- GET ALL ----------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllMessages_shouldReturnList() throws Exception {

        MessageDto message = MessageDto.builder()
                .content("Hello World")
                .build();

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

        MessageDto message = MessageDto.builder()
                .content("Paged Message")
                .build();

        Page<MessageDto> page =
                new PageImpl<>(Collections.singletonList(message));

        when(messageService.getMessagesPaginated(
                0,
                10,
                "timestamp",
                "desc"))
                .thenReturn(page);

        mockMvc.perform(get("/api/messages/paged")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "timestamp")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content")
                        .value("Paged Message"));
    }

    // ---------------------- GET BY ID ----------------------

    @Test
    void getMessageById_shouldReturnMessage_whenFound()
            throws Exception {

        MessageDto message = MessageDto.builder()
                .id(1L)
                .content("Test Message")
                .timestamp(LocalDateTime.now())
                .build();

        when(messageService.getMessageByIdForUser(1L, "alice", true))
                .thenReturn(message);

        mockMvc.perform(get("/api/message/1")
                        .with(jwt().jwt(token -> token
                                .claim("preferred_username", "alice")
                                .claim("realm_access", Map.of(
                                        "roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Test Message"));
    }

    @Test
    void getMessageById_shouldReturnNotFound_whenMissing()
            throws Exception {

        doThrow(new MessageNotFoundException(
                "Message with ID 999 not found"))
                .when(messageService)
                .getMessageByIdForUser(999L, "alice", true);

        mockMvc.perform(get("/api/message/999")
                        .with(jwt().jwt(token -> token
                                .claim("preferred_username", "alice")
                                .claim("realm_access", Map.of(
                                        "roles", List.of("ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }

    // ---------------------- CREATE ----------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void createMessage_shouldReturnCreated() throws Exception {

        MessageRequest request = MessageRequest.builder()
                .content("New Message")
                .sender("Alice")
                .receiver("Bob")
                .build();

        MessageDto response = MessageDto.builder()
                .content("New Message")
                .sender("Alice")
                .receiver("Bob")
                .build();

        when(messageService.saveMessage(any(MessageRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/message")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content")
                        .value("New Message"))
                .andExpect(jsonPath("$.sender")
                        .value("Alice"))
                .andExpect(jsonPath("$.receiver")
                        .value("Bob"));
    }

    // ---------------------- DELETE ----------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteMessage_shouldReturnNoContent_whenExists()
            throws Exception {

        doNothing()
                .when(messageService)
                .deleteMessage(1L);

        mockMvc.perform(delete("/api/message/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteMessage_shouldReturnNotFound_whenMissing()
            throws Exception {

        doThrow(new MessageNotFoundException(
                "Message with ID 999 not found"))
                .when(messageService)
                .deleteMessage(999L);

        mockMvc.perform(delete("/api/message/999"))
                .andExpect(status().isNotFound());
    }

    // ---------------------- SEND MESSAGE ----------------------

    @Test
    void sendMessage_shouldUseAuthenticatedSenderInsteadOfSpoofedSender()
            throws Exception {

        MessageRequest request = MessageRequest.builder()
                .content("Hello")
                .sender("mallory")
                .receiver("bob")
                .build();

        MessageDto response = MessageDto.builder()
                .content("Hello")
                .sender("alice")
                .receiver("bob")
                .build();

        when(recipientDirectoryClient.getEligibleRecipients("test-token"))
                .thenReturn(List.of("bob"));
        when(messageService.sendMessage(any(MessageRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/message/send")
                        .with(jwt().jwt(token -> token
                                .tokenValue("test-token")
                                .claim("preferred_username", "alice")
                                .claim("realm_access", Map.of(
                                        "roles", List.of("USER"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Hello"))
                .andExpect(jsonPath("$.sender").value("alice"))
                .andExpect(jsonPath("$.receiver").value("bob"));

        org.mockito.ArgumentCaptor<MessageRequest> captor =
                org.mockito.ArgumentCaptor.forClass(MessageRequest.class);
        org.mockito.Mockito.verify(messageService).sendMessage(captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals("alice", captor.getValue().getSender());
        org.mockito.Mockito.verify(recipientDirectoryClient)
                .getEligibleRecipients("test-token");
    }

    @Test
    void sendMessage_shouldRejectRecipientNotInEligibleDirectory()
            throws Exception {

        MessageRequest request = MessageRequest.builder()
                .content("Hello")
                .sender("forged-sender")
                .receiver("bob")
                .build();

        when(recipientDirectoryClient.getEligibleRecipients("test-token"))
                .thenReturn(List.of("charlie"));

        mockMvc.perform(post("/api/message/send")
                        .with(jwt().jwt(token -> token
                                .tokenValue("test-token")
                                .claim("preferred_username", "alice")
                                .claim("realm_access", Map.of(
                                        "roles", List.of("USER"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        org.mockito.Mockito.verify(messageService, org.mockito.Mockito.never())
                .sendMessage(any(MessageRequest.class));
    }

    @Test
    void sendMessage_shouldRejectTokenWithBothMessagingRoles()
            throws Exception {

        MessageRequest request = MessageRequest.builder()
                .content("Hello")
                .receiver("bob")
                .build();

        mockMvc.perform(post("/api/message/send")
                        .with(jwt().jwt(token -> token
                                .claim("preferred_username", "alice")
                                .claim("realm_access", Map.of(
                                        "roles", List.of("USER", "ADMIN"))))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"),
                                        new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        org.mockito.Mockito.verify(recipientDirectoryClient, org.mockito.Mockito.never())
                .getEligibleRecipients(any());
        org.mockito.Mockito.verify(messageService, org.mockito.Mockito.never())
                .sendMessage(any(MessageRequest.class));
    }


    // ---------------------- SECURITY AND VALIDATION ----------------------

    @Test
    @WithMockUser(roles = "USER")
    void userRole_shouldBeForbiddenFromAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isForbidden());
    }
    @Test
    void unauthenticatedRequest_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void createMessage_shouldRejectBlankRequiredFields() throws Exception {
        mockMvc.perform(post("/api/message")
                        .contentType("application/json")
                        .content("{\"content\":\" \",\"sender\":\"\",\"receiver\":\"Bob\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void createMessage_shouldRejectContentLongerThan500Characters() throws Exception {
        MessageRequest request = MessageRequest.builder()
                .content("x".repeat(501))
                .sender("Alice")
                .receiver("Bob")
                .build();

        mockMvc.perform(post("/api/message")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
    @Test
    void getMyMessages_shouldUseAuthenticatedUsername() throws Exception {
        when(messageService.getMessagesForUser("alice"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/messages/my")
                        .with(jwt().jwt(token ->
                                token.claim("preferred_username", "alice"))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        org.mockito.Mockito.verify(messageService)
                .getMessagesForUser("alice");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unsupportedHttpMethod_shouldReturnMethodNotAllowed() throws Exception {
        mockMvc.perform(patch("/api/messages"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error").value("Method Not Allowed"));
    }
}
