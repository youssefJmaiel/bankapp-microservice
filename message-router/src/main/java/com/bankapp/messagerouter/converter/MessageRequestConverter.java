package com.bankapp.messagerouter.converter;

import com.bankapp.message.dto.MessageRequest;
import com.bankapp.messagerouter.entity.Message;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MessageRequestConverter {

    public Message toEntity(MessageRequest request) {
        if (request == null) {
            return null;
        }

        Message message = new Message();
        message.setContent(request.getContent());
        message.setSender(request.getSender());
        message.setReceiver(request.getReceiver());
        message.setTimestamp(LocalDateTime.now());
        message.setProcessed(false);

        return message;
    }
}
