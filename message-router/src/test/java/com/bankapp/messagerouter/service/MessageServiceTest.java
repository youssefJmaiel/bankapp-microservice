package com.bankapp.messagerouter.service;

import com.bankapp.message.dto.MessageDto;
import com.bankapp.message.dto.MessageRequest;
import com.bankapp.messagerouter.converter.MessageDtoConverter;
import com.bankapp.messagerouter.converter.MessageRequestConverter;
import com.bankapp.messagerouter.entity.Message;
import com.bankapp.messagerouter.error.MessageNotFoundException;
import com.bankapp.messagerouter.repository.MessageRepository;
import org.springframework.security.access.AccessDeniedException;
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
        objectMapper = new ObjectMapper().findAndRegisterModules();

        MessageDtoConverter messageDtoConverter =
                new MessageDtoConverter();

        MessageRequestConverter messageRequestConverter =
                new MessageRequestConverter();

        messageService = new MessageService(
                messageRepository,
                mqService,
                objectMapper,
                messageDtoConverter,
                messageRequestConverter
        );
    }

    @Test
    void getAllMessages_shouldReturnAllMessages() {

        Message m1 = new Message();
        m1.setId(1L);
        m1.setContent("Message 1");

        Message m2 = new Message();
        m2.setId(2L);
        m2.setContent("Message 2");

        when(messageRepository.findAll())
                .thenReturn(Arrays.asList(m1, m2));

        List<MessageDto> messages =
                messageService.getAllMessages();

        assertEquals(2, messages.size());
        assertEquals(1L, messages.get(0).getId());
        assertEquals("Message 1", messages.get(0).getContent());

        verify(messageRepository, times(1))
                .findAll();
    }

    @Test
    void getMessagesPaginated_shouldReturnPage() {

        Message m1 = new Message();
        m1.setId(1L);
        m1.setContent("Message 1");

        Message m2 = new Message();
        m2.setId(2L);
        m2.setContent("Message 2");

        Page<Message> page =
                new PageImpl<>(Arrays.asList(m1, m2));

        when(messageRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<MessageDto> result =
                messageService.getMessagesPaginated(
                        0,
                        2,
                        "id",
                        "asc"
                );

        assertEquals(2, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());

        verify(messageRepository, times(1))
                .findAll(any(Pageable.class));
    }

    @Test
    void saveMessage_shouldSaveAndReturnDto() {

        MessageRequest request =
                MessageRequest.builder()
                        .content("Hello")
                        .sender("Alice")
                        .receiver("Bob")
                        .build();

        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        MessageDto saved =
                messageService.saveMessage(request);

        assertEquals("Hello", saved.getContent());
        assertEquals("Alice", saved.getSender());
        assertEquals("Bob", saved.getReceiver());
        assertFalse(saved.isProcessed());
        assertNotNull(saved.getTimestamp());

        verify(messageRepository, times(1))
                .save(any(Message.class));
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

        verify(messageRepository, never())
                .deleteById(anyLong());
    }

    @Test
    void getMessageById_shouldReturnDto_whenFound() {

        Message message = new Message();
        message.setId(1L);
        message.setContent("Test Message");

        when(messageRepository.findById(1L))
                .thenReturn(Optional.of(message));

        MessageDto found =
                messageService.getMessageById(1L);

        assertEquals(1L, found.getId());
        assertEquals("Test Message", found.getContent());
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

        MessageRequest request =
                MessageRequest.builder()
                        .content("Hi")
                        .sender("Alice")
                        .receiver("Bob")
                        .build();

        ArgumentCaptor<Message> captor =
                ArgumentCaptor.forClass(Message.class);

        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        MessageDto sent =
                messageService.sendMessage(request);

        verify(messageRepository)
                .save(captor.capture());

        Message captured = captor.getValue();

        assertEquals("Hi", captured.getContent());
        assertEquals("Alice", captured.getSender());
        assertEquals("Bob", captured.getReceiver());
        assertFalse(captured.isProcessed());
        assertNotNull(captured.getTimestamp());

        assertEquals("Hi", sent.getContent());
        assertEquals("Alice", sent.getSender());
        assertEquals("Bob", sent.getReceiver());

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
        message.setTimestamp(
                LocalDateTime.of(2026, 9, 26, 23, 0)
        );
        message.setProcessed(false);

        when(messageRepository.save(any(Message.class)))
                .thenReturn(message);

        MessageRequest request =
                MessageRequest.builder()
                        .content("Hello MQ")
                        .sender("Alice")
                        .receiver("Bob")
                        .build();

        MessageDto result =
                messageService.sendMessage(request);

        ArgumentCaptor<String> mqCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(mqService)
                .sendMessage(mqCaptor.capture());

        String json = mqCaptor.getValue();

        assertTrue(json.contains("\"id\":10"));
        assertTrue(json.contains("\"content\":\"Hello MQ\""));
        assertTrue(json.contains("\"sender\":\"Alice\""));
        assertTrue(json.contains("\"receiver\":\"Bob\""));

        assertEquals(10L, result.getId());
        assertEquals("Hello MQ", result.getContent());
        assertEquals("Alice", result.getSender());
        assertEquals("Bob", result.getReceiver());
    }


    @Test
    void getMessagesForReceiver_shouldReturnOnlyMessagesForReceiver() {
        Message message = new Message();
        message.setId(21L);
        message.setContent("Private message");
        message.setSender("bob");
        message.setReceiver("alice");

        when(messageRepository.findByReceiver("alice"))
                .thenReturn(List.of(message));

        List<MessageDto> result =
                messageService.getMessagesForReceiver("alice");

        assertEquals(1, result.size());
        assertEquals(21L, result.get(0).getId());
        assertEquals("Private message", result.get(0).getContent());
        verify(messageRepository).findByReceiver("alice");
    }

    @Test
    void getMessagesForReceiver_shouldReturnEmptyListWhenNoMessagesExist() {
        when(messageRepository.findByReceiver("alice"))
                .thenReturn(List.of());

        List<MessageDto> result =
                messageService.getMessagesForReceiver("alice");

        assertTrue(result.isEmpty());
        verify(messageRepository).findByReceiver("alice");
    }

    @Test
    void getMessagesForUser_shouldReturnMessagesSentAndReceivedByUser() {
        Message received = new Message();
        received.setId(31L);
        received.setContent("Received");
        received.setSender("bob");
        received.setReceiver("alice");

        Message sent = new Message();
        sent.setId(32L);
        sent.setContent("Sent");
        sent.setSender("alice");
        sent.setReceiver("charlie");

        when(messageRepository.findByReceiverOrSender("alice", "alice"))
                .thenReturn(List.of(received, sent));

        List<MessageDto> result = messageService.getMessagesForUser("alice");

        assertEquals(2, result.size());
        assertEquals("Received", result.get(0).getContent());
        assertEquals("Sent", result.get(1).getContent());
        verify(messageRepository).findByReceiverOrSender("alice", "alice");
    }

    @Test
    void getMessagesForUser_shouldRejectBlankUsername() {
        assertThrows(
                AccessDeniedException.class,
                () -> messageService.getMessagesForUser(" ")
        );

        verify(messageRepository, never())
                .findByReceiverOrSender(anyString(), anyString());
    }

    @Test
    void getMessageByIdForUser_shouldAllowSender() {
        Message message = new Message();
        message.setId(41L);
        message.setContent("Private");
        message.setSender("alice");
        message.setReceiver("bob");

        when(messageRepository.findById(41L)).thenReturn(Optional.of(message));

        MessageDto result = messageService.getMessageByIdForUser(41L, "alice", false);

        assertEquals("Private", result.getContent());
    }

    @Test
    void getMessageByIdForUser_shouldAllowReceiver() {
        Message message = new Message();
        message.setId(42L);
        message.setContent("Private");
        message.setSender("alice");
        message.setReceiver("bob");

        when(messageRepository.findById(42L)).thenReturn(Optional.of(message));

        MessageDto result = messageService.getMessageByIdForUser(42L, "bob", false);

        assertEquals("Private", result.getContent());
    }

    @Test
    void getMessageByIdForUser_shouldRejectNonParticipant() {
        Message message = new Message();
        message.setId(43L);
        message.setContent("Private");
        message.setSender("alice");
        message.setReceiver("bob");

        when(messageRepository.findById(43L)).thenReturn(Optional.of(message));

        assertThrows(
                AccessDeniedException.class,
                () -> messageService.getMessageByIdForUser(43L, "charlie", false)
        );
    }

    @Test
    void getMessageByIdForUser_shouldAllowAdminWhoIsNotParticipant() {
        Message message = new Message();
        message.setId(44L);
        message.setContent("Administrative view");
        message.setSender("alice");
        message.setReceiver("bob");

        when(messageRepository.findById(44L)).thenReturn(Optional.of(message));

        MessageDto result = messageService.getMessageByIdForUser(44L, "admin", true);

        assertEquals("Administrative view", result.getContent());
    }

    @Test
    void getMessagesPaginated_shouldUseDescendingSortWhenDirectionIsDesc()
            {
        when(messageRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        messageService.getMessagesPaginated(0, 10, "timestamp", "desc");

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);
        verify(messageRepository).findAll(captor.capture());

        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(10, captor.getValue().getPageSize());
        assertEquals(
                Sort.Direction.DESC,
                captor.getValue().getSort().getOrderFor("timestamp").getDirection()
        );
    }

    @Test
    void getMessagesPaginated_shouldUseAscendingSortWhenDirectionIsAsc() {
        when(messageRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        messageService.getMessagesPaginated(1, 5, "id", "asc");

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);
        verify(messageRepository).findAll(captor.capture());

        assertEquals(1, captor.getValue().getPageNumber());
        assertEquals(5, captor.getValue().getPageSize());
        assertEquals(
                Sort.Direction.ASC,
                captor.getValue().getSort().getOrderFor("id").getDirection()
        );
    }

}
