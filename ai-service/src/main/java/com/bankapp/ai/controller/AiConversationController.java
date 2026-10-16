package com.bankapp.ai.controller;

import com.bankapp.ai.domain.AiConversationDto;
import com.bankapp.ai.domain.AiConversationSummaryDto;
import com.bankapp.ai.service.AiConversationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/conversations")
public class AiConversationController {

    private final AiConversationService conversationService;

    public AiConversationController(AiConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping
    public ResponseEntity<AiConversationDto> create(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(conversationService.createConversation(jwt.getSubject()));
    }

    @GetMapping
    public List<AiConversationSummaryDto> list(
            @AuthenticationPrincipal Jwt jwt) {
        return conversationService.listConversations(jwt.getSubject());
    }

    @GetMapping("/{id}")
    public AiConversationDto get(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return conversationService.getConversation(id, jwt.getSubject());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Jwt jwt) {
        conversationService.deleteConversation(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}
