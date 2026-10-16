package com.bankapp.ai.repository;

import com.bankapp.ai.entity.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiConversationRepository
        extends JpaRepository<AiConversation, Long> {

    List<AiConversation> findByOwnerSubjectOrderByUpdatedAtDesc(
            String ownerSubject);

    Optional<AiConversation> findByIdAndOwnerSubject(
            Long id, String ownerSubject);
}
