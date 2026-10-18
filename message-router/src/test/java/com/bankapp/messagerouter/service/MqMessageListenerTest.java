package com.bankapp.messagerouter.service;

import com.bankapp.messagerouter.entity.Message;
import com.bankapp.messagerouter.repository.MessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MqMessageListenerTest {

    @Mock
    private MessageRepository messageRepository;

    private MqMessageListener listener;

    @BeforeEach
    void setUp() {
        listener = new MqMessageListener(messageRepository, new ObjectMapper());
    }

    @Test
    void handleMessageWhenMessageExistsMarksItAsProcessedAndSavesIt() {
        Message message = new Message();
        message.setId(42L);
        message.setProcessed(false);

        when(messageRepository.findById(42L)).thenReturn(Optional.of(message));

        listener.handleMessage("{\"id\":42}");

        assertTrue(message.isProcessed());
        verify(messageRepository).findById(42L);
        verify(messageRepository).save(message);
    }

    @Test
    void handleMessageWhenIdIsMissingDoesNotAccessRepository() {
        listener.handleMessage("{\"content\":\"hello\"}");

        verifyNoInteractions(messageRepository);
    }

    @Test
    void handleMessageWhenIdIsNullDoesNotAccessRepository() {
        listener.handleMessage("{\"id\":null}");

        verifyNoInteractions(messageRepository);
    }

    @Test
    void handleMessageWhenMessageDoesNotExistDoesNotSaveAnything() {
        when(messageRepository.findById(99L)).thenReturn(Optional.empty());

        listener.handleMessage("{\"id\":99}");

        verify(messageRepository).findById(99L);
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void handleMessageWhenJsonIsInvalidDoesNotPropagateException() {
        listener.handleMessage("{invalid-json");

        verifyNoInteractions(messageRepository);
    }

    @Test
    void handleMessageWhenRepositoryThrowsDoesNotPropagateException() {
        when(messageRepository.findById(42L))
                .thenThrow(new RuntimeException("Database unavailable"));

        listener.handleMessage("{\"id\":42}");

        verify(messageRepository).findById(42L);
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void handleMessageWhenSaveThrowsDoesNotPropagateException() {
        Message message = new Message();
        message.setId(42L);
        message.setProcessed(false);

        when(messageRepository.findById(42L)).thenReturn(Optional.of(message));
        when(messageRepository.save(message))
                .thenThrow(new RuntimeException("Save failed"));

        listener.handleMessage("{\"id\":42}");

        assertTrue(message.isProcessed());
        verify(messageRepository).save(message);
    }
}
