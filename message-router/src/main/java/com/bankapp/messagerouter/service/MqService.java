package com.bankapp.messagerouter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.Queue;

@Slf4j
@Service
public class MqService {

    private final JmsTemplate jmsTemplate;
    private final Queue queue;

    public MqService(JmsTemplate jmsTemplate, Queue queue) {
        this.jmsTemplate = jmsTemplate;
        this.queue = queue;
    }

    public void connect() {
        try {
            jmsTemplate.setReceiveTimeout(10000); // timeout 10s
            log.info("Connected to MQ with timeout 10000ms");
        } catch (Exception e) {
            log.error("Error connecting to MQ", e);
            throw new RuntimeException("Timeout error", e);
        }
    }

    public void sendMessage(String message) {
        try {
            jmsTemplate.convertAndSend(queue, message);
            log.info("Message sent to MQ: {}", message);
        } catch (Exception e) {
            log.error("Error sending message to MQ", e);
            throw new RuntimeException("MQ send failed", e);
        }
    }

    public String receiveMessage() {
        try {
            Object msg = jmsTemplate.receiveAndConvert(queue);
            if (msg != null) {
                log.info("Message received from MQ: {}", msg);
                return msg.toString();
            } else {
                log.info("No message received from MQ");
                return null;
            }
        } catch (Exception e) {
            log.error("Error receiving message from MQ", e);
            throw new RuntimeException("MQ receive failed", e);
        }
    }
}