package com.bankapp.ai.controller;

import com.bankapp.ai.domain.ChatRequest;
import com.bankapp.ai.domain.ChatResponse;
import com.bankapp.ai.service.ChatService;
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

    public AiChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(
            value = "/chat",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ChatResponse chat(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader(value = "X-Response-Language", required = false) String language,
            @AuthenticationPrincipal Jwt jwt) {
        return chatService.chat(request.getMessage(), language, jwt.getTokenValue());
    }
}
