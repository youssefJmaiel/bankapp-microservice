package com.bankapp.messagerouter.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Queue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MqServiceTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private Queue queue;

    @InjectMocks
    private MqService mqService;

    @Test
    void testConnect() {
        mqService.connect();
        verify(jmsTemplate).setReceiveTimeout(anyLong());
    }

    @Test
    void testConnectWithException() {
        doThrow(new RuntimeException("Timeout error")).when(jmsTemplate).setReceiveTimeout(anyLong());

        RuntimeException exception = assertThrows(RuntimeException.class, mqService::connect);
        assertEquals("Timeout error", exception.getMessage());
    }

    @Test
    void testSendMessage() {
        mqService.sendMessage("Test message");
        verify(jmsTemplate).convertAndSend(queue, "Test message");
    }

    @Test
    void testSendMessageThrowsException() {
        doThrow(new RuntimeException("MQ down")).when(jmsTemplate).convertAndSend(queue, "Fail message");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> mqService.sendMessage("Fail message"));
        assertEquals("MQ send failed", exception.getMessage());
    }

    @Test
    void testReceiveMessage() {
        when(jmsTemplate.receiveAndConvert(queue)).thenReturn("Hello MQ");

        String msg = mqService.receiveMessage();
        assertEquals("Hello MQ", msg);
    }

    @Test
    void testReceiveMessageNull() {
        when(jmsTemplate.receiveAndConvert(queue)).thenReturn(null);

        String msg = mqService.receiveMessage();
        assertNull(msg);
    }

    @Test
    void testReceiveMessageThrowsException() {
        when(jmsTemplate.receiveAndConvert(queue)).thenThrow(new RuntimeException("MQ error"));

        RuntimeException exception = assertThrows(RuntimeException.class, mqService::receiveMessage);
        assertEquals("MQ receive failed", exception.getMessage());
    }
}