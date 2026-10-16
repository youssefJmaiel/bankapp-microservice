package com.bankapp.ai.service;

import com.bankapp.ai.domain.AiConversationDto;
import com.bankapp.ai.domain.AiConversationSummaryDto;
import com.bankapp.ai.domain.AiMessageDto;
import com.bankapp.ai.entity.AiConversation;
import com.bankapp.ai.entity.AiMessage;
import com.bankapp.ai.entity.MessageRole;
import com.bankapp.ai.repository.AiConversationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiConversationService {

    private static final String DEFAULT_TITLE = "Nouvelle conversation";

    private final AiConversationRepository conversationRepository;

    public AiConversationService(
            AiConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    @Transactional
    public AiConversationDto createConversation(String ownerSubject) {
        requireOwner(ownerSubject);
        AiConversation conversation =
                conversationRepository.save(
                        new AiConversation(ownerSubject, DEFAULT_TITLE));
        return toDto(conversation);
    }

    @Transactional(readOnly = true)
    public List<AiConversationSummaryDto> listConversations(
            String ownerSubject) {
        requireOwner(ownerSubject);
        return conversationRepository
                .findByOwnerSubjectOrderByUpdatedAtDesc(ownerSubject)
                .stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AiConversationDto getConversation(
            Long conversationId, String ownerSubject) {
        return toDto(findOwned(conversationId, ownerSubject));
    }

    @Transactional
    public void deleteConversation(Long conversationId, String ownerSubject) {
        conversationRepository.delete(findOwned(conversationId, ownerSubject));
    }

    @Transactional
    public Long appendUserMessage(
            Long conversationId, String ownerSubject, String content) {
        requireOwner(ownerSubject);

        AiConversation conversation;
        if (conversationId == null) {
            conversation = new AiConversation(
                    ownerSubject, makeTitle(content));
            conversation = conversationRepository.save(conversation);
        } else {
            conversation = findOwned(conversationId, ownerSubject);
            if (DEFAULT_TITLE.equals(conversation.getTitle())) {
                conversation.setTitle(makeTitle(content));
            }
        }

        conversation.addMessage(new AiMessage(MessageRole.USER, content));
        conversation.touch();
        conversationRepository.save(conversation);
        return conversation.getId();
    }

    @Transactional
    public void appendAssistantMessage(
            Long conversationId, String ownerSubject, String content) {
        AiConversation conversation = findOwned(conversationId, ownerSubject);
        conversation.addMessage(new AiMessage(MessageRole.ASSISTANT, content));
        conversation.touch();
        conversationRepository.save(conversation);
    }

    private AiConversationDto toDto(AiConversation conversation) {
        List<AiMessageDto> messages = conversation.getMessages().stream()
                .map(message -> new AiMessageDto(
                        message.getId(),
                        message.getRole().name(),
                        message.getContent(),
                        message.getCreatedAt()))
                .collect(Collectors.toList());

        return new AiConversationDto(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                messages);
    }

    private AiConversationSummaryDto toSummaryDto(
            AiConversation conversation) {
        return new AiConversationSummaryDto(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                conversation.getMessages().size());
    }

    private AiConversation findOwned(Long conversationId, String ownerSubject) {
        requireOwner(ownerSubject);
        if (conversationId == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Conversation introuvable.");
        }
        return conversationRepository
                .findByIdAndOwnerSubject(conversationId, ownerSubject)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Conversation introuvable."));
    }

    private void requireOwner(String ownerSubject) {
        if (ownerSubject == null || ownerSubject.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Identité utilisateur absente.");
        }
    }

    private String makeTitle(String content) {
        if (content == null || content.trim().isEmpty()) {
            return DEFAULT_TITLE;
        }
        String normalized = content.trim().replaceAll("\\s+", " ");
        return normalized.length() <= 120
                ? normalized
                : normalized.substring(0, 117) + "...";
    }
}
