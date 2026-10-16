package com.bankapp.ai.domain;

import java.time.LocalDateTime;

public class AiConversationSummaryDto {
    private Long id;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int messageCount;

    public AiConversationSummaryDto() {
    }

    public AiConversationSummaryDto(
            Long id,
            String title,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            int messageCount) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.messageCount = messageCount;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public int getMessageCount() { return messageCount; }
}
