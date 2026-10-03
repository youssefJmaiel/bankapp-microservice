package com.bankapp.messagerouter.service;

import com.bankapp.messagerouter.entity.Message;
import com.bankapp.messagerouter.error.MessageNotFoundException;
import com.bankapp.messagerouter.repository.MessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final MqService mqService;
    private final ObjectMapper objectMapper;

    public MessageService(
            MessageRepository messageRepository,
            MqService mqService,
            ObjectMapper objectMapper) {

        this.messageRepository = messageRepository;
        this.mqService = mqService;
        this.objectMapper = objectMapper;
    }

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    public Page<Message> getMessagesPaginated(
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return messageRepository.findAll(pageable);
    }

    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

    public void deleteMessage(Long id) {
        if (messageRepository.existsById(id)) {
            messageRepository.deleteById(id);
        } else {
            throw new MessageNotFoundException(
                    "Message with ID " + id + " not found"
            );
        }
    }

    public Message getMessageById(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new MessageNotFoundException(
                        "Message with ID " + id + " not found"
                ));
    }

    public Message sendMessage(
            String content,
            String sender,
            String receiver) {

        // 1. Create the message
        Message message = new Message();
        message.setContent(content);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setTimestamp(LocalDateTime.now());
        message.setProcessed(false);

        // 2. Save it in the database first
        Message savedMessage = messageRepository.save(message);

        // 3. Build the MQ payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", savedMessage.getId());
        payload.put("content", savedMessage.getContent());
        payload.put("sender", savedMessage.getSender());
        payload.put("receiver", savedMessage.getReceiver());
        payload.put("timestamp", savedMessage.getTimestamp());

        try {
            // 4. Convert the message to JSON
            String jsonMessage = objectMapper.writeValueAsString(payload);

            // 5. Send the message to IBM MQ
            mqService.sendMessage(jsonMessage);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                    "Failed to serialize message for IBM MQ",
                    e
            );
        }

        return savedMessage;
    }
}
