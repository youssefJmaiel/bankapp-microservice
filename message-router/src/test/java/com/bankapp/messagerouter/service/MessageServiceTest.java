package com.bankapp.messagerouter.service;

import com.bankapp.messagerouter.entity.Message;
import com.bankapp.messagerouter.error.MessageNotFoundException;
import com.bankapp.messagerouter.repository.MessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageServiceTest {

    private MessageRepository messageRepository;
    private MqService mqService;
    private ObjectMapper objectMapper;
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageRepository = mock(MessageRepository.class);
        mqService = mock(MqService.class);
        objectMapper = new ObjectMapper();

        messageService = new MessageService(
                messageRepository,
                mqService,
                objectMapper
        );
    }

    @Test
    void getAllMessages_shouldReturnAllMessages() {
        Message m1 = new Message();
        m1.setId(1L);

        Message m2 = new Message();
        m2.setId(2L);

        when(messageRepository.findAll())
                .thenReturn(Arrays.asList(m1, m2));

        List<Message> messages = messageService.getAllMessages();

        assertEquals(2, messages.size());
        verify(messageRepository, times(1)).findAll();
    }

    @Test
    void getMessagesPaginated_shouldReturnPage() {
        Message m1 = new Message();
        m1.setId(1L);

        Message m2 = new Message();
        m2.setId(2L);

        Page<Message> page =
                new PageImpl<>(Arrays.asList(m1, m2));

        when(messageRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<Message> result =
                messageService.getMessagesPaginated(
                        0,
                        2,
                        "id",
                        "asc"
                );

        assertEquals(2, result.getContent().size());

        verify(messageRepository, times(1))
                .findAll(any(Pageable.class));
    }

    @Test
    void saveMessage_shouldSaveAndReturnMessage() {
        Message message = new Message();
        message.setContent("Hello");

        when(messageRepository.save(message))
                .thenReturn(message);

        Message saved =
                messageService.saveMessage(message);

        assertEquals("Hello", saved.getContent());

        verify(messageRepository, times(1))
                .save(message);
    }

    @Test
    void deleteMessage_shouldCallRepository_whenMessageExists() {
        when(messageRepository.existsById(1L))
                .thenReturn(true);

        messageService.deleteMessage(1L);

        verify(messageRepository, times(1))
                .deleteById(1L);
    }

    @Test
    void deleteMessage_shouldThrowException_whenMessageNotFound() {
        when(messageRepository.existsById(3L))
                .thenReturn(false);

        MessageNotFoundException ex =
                assertThrows(
                        MessageNotFoundException.class,
                        () -> messageService.deleteMessage(3L)
                );

        assertEquals(
                "Message with ID 3 not found",
                ex.getMessage()
        );
    }

    @Test
    void getMessageById_shouldReturnMessage_whenFound() {
        Message message = new Message();
        message.setId(1L);

        when(messageRepository.findById(1L))
                .thenReturn(Optional.of(message));

        Message found =
                messageService.getMessageById(1L);

        assertEquals(1L, found.getId());
    }

    @Test
    void getMessageById_shouldThrowException_whenNotFound() {
        when(messageRepository.findById(2L))
                .thenReturn(Optional.empty());

        MessageNotFoundException ex =
                assertThrows(
                        MessageNotFoundException.class,
                        () -> messageService.getMessageById(2L)
                );

        assertEquals(
                "Message with ID 2 not found",
                ex.getMessage()
        );
    }

    @Test
    void sendMessage_shouldSaveMessageWithTimestampAndProcessedFalse() {
        String content = "Hi";
        String sender = "Alice";
        String receiver = "Bob";

        ArgumentCaptor<Message> captor =
                ArgumentCaptor.forClass(Message.class);

        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Message sent =
                messageService.sendMessage(
                        content,
                        sender,
                        receiver
                );

        verify(messageRepository)
                .save(captor.capture());

        Message captured = captor.getValue();

        assertEquals(content, captured.getContent());
        assertEquals(sender, captured.getSender());
        assertEquals(receiver, captured.getReceiver());
        assertFalse(captured.isProcessed());
        assertNotNull(captured.getTimestamp());

        assertEquals(captured, sent);

        verify(mqService, times(1))
                .sendMessage(anyString());
    }

    @Test
    void sendMessage_shouldSendJsonToMq() {
        Message message = new Message();

        message.setId(10L);
        message.setContent("Hello MQ");
        message.setSender("Alice");
        message.setReceiver("Bob");
        message.setTimestamp(LocalDateTime.of(2026, 9, 26, 23, 0));
        message.setProcessed(false);

        when(messageRepository.save(any(Message.class)))
                .thenReturn(message);

        ArgumentCaptor<String> mqCaptor =
                ArgumentCaptor.forClass(String.class);

        Message result =
                messageService.sendMessage(
                        "Hello MQ",
                        "Alice",
                        "Bob"
                );

        verify(mqService)
                .sendMessage(mqCaptor.capture());

        String json = mqCaptor.getValue();

        assertTrue(json.contains("\"id\":10"));
        assertTrue(json.contains("\"content\":\"Hello MQ\""));
        assertTrue(json.contains("\"sender\":\"Alice\""));
        assertTrue(json.contains("\"receiver\":\"Bob\""));

        assertEquals(message, result);
    }
}
