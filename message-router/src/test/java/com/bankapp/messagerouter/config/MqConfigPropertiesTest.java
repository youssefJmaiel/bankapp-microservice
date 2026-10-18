package com.bankapp.messagerouter.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MqConfigPropertiesTest {

    @Test
    void propertiesCanBeSetAndRetrieved() {
        MqConfigProperties properties = new MqConfigProperties();

        assertNull(properties.getQueueName());
        assertNull(properties.getHost());
        assertEquals(0, properties.getPort());

        properties.setQueueName("BANKAPP.QUEUE");
        properties.setHost("localhost");
        properties.setPort(1414);

        assertEquals("BANKAPP.QUEUE", properties.getQueueName());
        assertEquals("localhost", properties.getHost());
        assertEquals(1414, properties.getPort());
    }
}
