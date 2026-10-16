package com.bankapp.ai.repository;

import com.bankapp.ai.entity.AiMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {

    List<AiMessage> findByConversation_IdOrderByCreatedAtAsc(
            Long conversationId);
}
