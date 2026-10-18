package com.bankapp.messagerouter.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    @Test
    void noArgsConstructor_shouldInitializeDefaultValues() {
        Message message = new Message();

        assertNull(message.getId());
        assertNull(message.getContent());
        assertNull(message.getSender());
        assertNull(message.getReceiver());
        assertNull(message.getTimestamp());
        assertFalse(message.isProcessed());
    }

    @Test
    void allArgsConstructor_shouldInitializeAllFields() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 10, 18, 12, 30);

        Message message = new Message(
                12L, "{\"type\":\"test\"}", "alice", "bob", timestamp, true);

        assertEquals(12L, message.getId());
        assertEquals("{\"type\":\"test\"}", message.getContent());
        assertEquals("alice", message.getSender());
        assertEquals("bob", message.getReceiver());
        assertEquals(timestamp, message.getTimestamp());
        assertTrue(message.isProcessed());
    }

    @Test
    void setters_shouldUpdateAllFields() {
        LocalDateTime timestamp = LocalDateTime.now();
        Message message = new Message();

        message.setId(7L);
        message.setContent("payload");
        message.setSender("sender");
        message.setReceiver("receiver");
        message.setTimestamp(timestamp);
        message.setProcessed(true);

        assertEquals(7L, message.getId());
        assertEquals("payload", message.getContent());
        assertEquals("sender", message.getSender());
        assertEquals("receiver", message.getReceiver());
        assertEquals(timestamp, message.getTimestamp());
        assertTrue(message.isProcessed());
    }

    @Test
    void equalsAndHashCode_shouldReflectFieldValues() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 10, 18, 12, 30);

        Message first = new Message(1L, "payload", "alice", "bob", timestamp, false);
        Message second = new Message(1L, "payload", "alice", "bob", timestamp, false);
        Message different = new Message(2L, "other", "alice", "bob", timestamp, false);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, different);
    }

    @Test
    void toString_shouldIncludeMessageFields() {
        Message message = new Message(
                3L, "hello", "alice", "bob",
                LocalDateTime.of(2026, 10, 18, 12, 30), true);

        String result = message.toString();

        assertTrue(result.contains("hello"));
        assertTrue(result.contains("alice"));
        assertTrue(result.contains("bob"));
        assertTrue(result.contains("processed=true"));
    }
}
