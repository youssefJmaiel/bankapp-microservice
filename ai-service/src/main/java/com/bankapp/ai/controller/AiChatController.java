package com.bankapp.ai.controller;

import com.bankapp.ai.domain.ChatRequest;
import com.bankapp.ai.domain.ChatResponse;
import com.bankapp.ai.service.ChatService;
import com.bankapp.ai.service.AiConversationService;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final ChatService chatService;
    private final AiConversationService conversationService;

    public AiChatController(ChatService chatService,
                            AiConversationService conversationService) {
        this.chatService = chatService;
        this.conversationService = conversationService;
    }

    @PostMapping(
            value = "/chat",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ChatResponse chat(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader(value = "X-Response-Language", required = false) String language,
            @RequestHeader(value = "X-Conversation-Id", required = false) Long conversationId,
            @AuthenticationPrincipal Jwt jwt) {
        Long savedConversationId = conversationService.appendUserMessage(
                conversationId, jwt.getSubject(), request.getMessage());
        ChatResponse response = chatService.chat(
                request.getMessage(), language, jwt.getTokenValue());
        conversationService.appendAssistantMessage(
                savedConversationId, jwt.getSubject(), response.getReply());
        response.setConversationId(savedConversationId);
        return response;
    }
}
