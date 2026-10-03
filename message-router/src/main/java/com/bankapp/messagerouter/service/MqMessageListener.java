package com.bankapp.messagerouter.service;

import com.bankapp.messagerouter.entity.Message;
import com.bankapp.messagerouter.repository.MessageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqMessageListener {

    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    public MqMessageListener(
            MessageRepository messageRepository,
            ObjectMapper objectMapper) {

        this.messageRepository = messageRepository;
        this.objectMapper = objectMapper;
    }

    public void handleMessage(String message) {
        log.info("Received message from MQ: {}", message);

        try {
            JsonNode jsonNode = objectMapper.readTree(message);

            JsonNode idNode = jsonNode.get("id");

            if (idNode == null || idNode.isNull()) {
                log.error("MQ message does not contain an id: {}", message);
                return;
            }

            Long messageId = idNode.asLong();

            Message databaseMessage = messageRepository
                    .findById(messageId)
                    .orElse(null);

            if (databaseMessage == null) {
                log.error(
                        "Message with ID {} was not found in database",
                        messageId
                );
                return;
            }

            databaseMessage.setProcessed(true);

            messageRepository.save(databaseMessage);

            log.info(
                    "Message with ID {} successfully processed and marked as processed=true",
                    messageId
            );

        } catch (Exception e) {
            log.error(
                    "Error processing message received from IBM MQ",
                    e
            );
        }
    }
}
