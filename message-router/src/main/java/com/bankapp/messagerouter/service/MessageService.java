package com.bankapp.messagerouter.service;

import com.bankapp.message.dto.MessageDto;
import com.bankapp.message.dto.MessageRequest;
import com.bankapp.messagerouter.converter.MessageDtoConverter;
import com.bankapp.messagerouter.converter.MessageRequestConverter;
import com.bankapp.messagerouter.entity.Message;
import com.bankapp.messagerouter.error.MessageNotFoundException;
import com.bankapp.messagerouter.repository.MessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final MqService mqService;
    private final ObjectMapper objectMapper;
    private final MessageDtoConverter messageDtoConverter;
    private final MessageRequestConverter messageRequestConverter;

    public MessageService(
            MessageRepository messageRepository,
            MqService mqService,
            ObjectMapper objectMapper,
            MessageDtoConverter messageDtoConverter,
            MessageRequestConverter messageRequestConverter) {

        this.messageRepository = messageRepository;
        this.mqService = mqService;
        this.objectMapper = objectMapper;
        this.messageDtoConverter = messageDtoConverter;
        this.messageRequestConverter = messageRequestConverter;
    }

    public List<MessageDto> getMessagesForReceiver(String receiver) {
        return messageRepository.findByReceiver(receiver)
                .stream()
                .map(messageDtoConverter::toDto)
                .collect(Collectors.toList());
    }

    public List<MessageDto> getAllMessages() {
        return messageRepository.findAll()
                .stream()
                .map(messageDtoConverter::toDto)
                .collect(Collectors.toList());
    }

    public Page<MessageDto> getMessagesPaginated(
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return messageRepository.findAll(pageable)
                .map(messageDtoConverter::toDto);
    }

    public MessageDto saveMessage(MessageRequest request) {
        Message message = messageRequestConverter.toEntity(request);
        Message savedMessage = messageRepository.save(message);

        return messageDtoConverter.toDto(savedMessage);
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

    public MessageDto getMessageById(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new MessageNotFoundException(
                        "Message with ID " + id + " not found"
                ));

        return messageDtoConverter.toDto(message);
    }

    public MessageDto sendMessage(MessageRequest request) {

        // 1. Convert the request to the JPA entity
        Message message = messageRequestConverter.toEntity(request);

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

        // 6. Return the DTO
        return messageDtoConverter.toDto(savedMessage);
    }
}
