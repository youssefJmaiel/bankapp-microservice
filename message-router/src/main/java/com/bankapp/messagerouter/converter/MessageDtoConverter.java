package com.bankapp.messagerouter.converter;

import com.bankapp.message.dto.MessageDto;
import com.bankapp.messagerouter.entity.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageDtoConverter {

    public MessageDto toDto(Message message) {
        if (message == null) {
            return null;
        }

        return MessageDto.builder()
                .id(message.getId())
                .content(message.getContent())
                .sender(message.getSender())
                .receiver(message.getReceiver())
                .timestamp(message.getTimestamp())
                .processed(message.isProcessed())
                .build();
    }
}
