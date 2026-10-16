package com.bankapp.ai.domain;

import java.time.LocalDateTime;

public class AiMessageDto {
    private Long id;
    private String role;
    private String content;
    private LocalDateTime createdAt;

    public AiMessageDto() {
    }

    public AiMessageDto(
            Long id, String role, String content, LocalDateTime createdAt) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getRole() { return role; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
