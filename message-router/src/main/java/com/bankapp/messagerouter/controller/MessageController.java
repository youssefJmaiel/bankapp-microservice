package com.bankapp.messagerouter.controller;

import com.bankapp.messagerouter.dto.MessageRequest;
import com.bankapp.messagerouter.entity.Message;
import com.bankapp.messagerouter.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@Slf4j
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/messages")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Message> getAllMessages() {
        return messageService.getAllMessages();
    }

    @GetMapping("/messages/paged")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Message>> getMessagesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        return ResponseEntity.ok(messageService.getMessagesPaginated(page, size, sortBy, direction));
    }

    @GetMapping("/message/{id}")
    public ResponseEntity<Message> getMessageById(@PathVariable Long id) {
        Message message = messageService.getMessageById(id); // lance MessageNotFoundException si absent
        return ResponseEntity.ok(message);
    }

    @PostMapping("/message")
    public ResponseEntity<Message> saveMessage(@RequestBody Message message) {
        return new ResponseEntity<>(messageService.saveMessage(message), HttpStatus.CREATED);
    }

    @DeleteMapping("/message/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id); // lance MessageNotFoundException si absent
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/message/send")
    public ResponseEntity<Message> sendMessage(@Valid @RequestBody MessageRequest messageRequest) {
        log.info("Received message request: {}", messageRequest);
        Message message = messageService.sendMessage(
                messageRequest.getContent(),
                messageRequest.getSender(),
                messageRequest.getReceiver()
        );
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }
}