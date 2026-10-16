package com.bankapp.ai.domain;

import java.time.LocalDateTime;
import java.util.List;

public class AiConversationDto {
    private Long id;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AiMessageDto> messages;

    public AiConversationDto() {
    }

    public AiConversationDto(
            Long id,
            String title,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<AiMessageDto> messages) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.messages = messages;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<AiMessageDto> getMessages() { return messages; }
}
