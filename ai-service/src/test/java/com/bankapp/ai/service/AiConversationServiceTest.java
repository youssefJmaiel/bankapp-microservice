package com.bankapp.ai.service;

import com.bankapp.ai.entity.AiConversation;
import com.bankapp.ai.entity.AiMessage;
import com.bankapp.ai.entity.MessageRole;
import org.springframework.test.util.ReflectionTestUtils;
import com.bankapp.ai.repository.AiConversationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiConversationServiceTest {

    @Mock
    private AiConversationRepository conversationRepository;

    @InjectMocks
    private AiConversationService conversationService;

    @Test
    void createsConversationForAuthenticatedOwner() {
        when(conversationRepository.save(any(AiConversation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = conversationService.createConversation("user-A");

        assertEquals("Nouvelle conversation", result.getTitle());
        verify(conversationRepository).save(any(AiConversation.class));
    }

    @Test
    void firstUserMessageCreatesConversationWithNormalizedTitle() {
        when(conversationRepository.save(any(AiConversation.class)))
                .thenAnswer(invocation -> {
                    AiConversation conversation = invocation.getArgument(0);
                    ReflectionTestUtils.setField(conversation, "id", 7L);
                    return conversation;
                });

        Long id = conversationService.appendUserMessage(
                null, "user-A", "  Bonjour   BankApp  ");

        assertEquals(7L, id);

        var captor = org.mockito.ArgumentCaptor.forClass(AiConversation.class);
        verify(conversationRepository, org.mockito.Mockito.atLeastOnce())
                .save(captor.capture());

        AiConversation saved = captor.getValue();
        assertEquals("user-A", saved.getOwnerSubject());
        assertEquals("Bonjour BankApp", saved.getTitle());
        assertEquals(1, saved.getMessages().size());

        AiMessage message = saved.getMessages().get(0);
        assertEquals(MessageRole.USER, message.getRole());
        assertEquals("  Bonjour   BankApp  ", message.getContent());
    }

    @Test
    void truncatesLongConversationTitleTo120Characters() {
        String content = "a".repeat(130);

        when(conversationRepository.save(any(AiConversation.class)))
                .thenAnswer(invocation -> {
                    AiConversation conversation = invocation.getArgument(0);
                    ReflectionTestUtils.setField(conversation, "id", 8L);
                    return conversation;
                });

        conversationService.appendUserMessage(null, "user-A", content);

        var captor = org.mockito.ArgumentCaptor.forClass(AiConversation.class);
        verify(conversationRepository, org.mockito.Mockito.atLeastOnce())
                .save(captor.capture());

        assertEquals(120, captor.getValue().getTitle().length());
        assertEquals("a".repeat(117) + "...", captor.getValue().getTitle());
    }

    @Test
    void cannotAppendMessageToConversationOwnedByAnotherUser() {
        when(conversationRepository.findByIdAndOwnerSubject(42L, "user-B"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> conversationService.appendUserMessage(
                        42L, "user-B", "message interdit"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(conversationRepository, never()).save(any(AiConversation.class));
    }

    @Test
    void rejectsMissingOwnerWhenCreatingConversation() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> conversationService.createConversation(" "));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        verify(conversationRepository, never()).save(any(AiConversation.class));
    }

    @Test
    void cannotReadConversationOwnedByAnotherUser() {
        when(conversationRepository.findByIdAndOwnerSubject(42L, "user-B"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> conversationService.getConversation(42L, "user-B"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void cannotDeleteConversationOwnedByAnotherUser() {
        when(conversationRepository.findByIdAndOwnerSubject(42L, "user-B"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> conversationService.deleteConversation(42L, "user-B"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(conversationRepository, never()).delete(any(AiConversation.class));
    }
}
