package com.bankapp.messagerouter.controller;

import com.bankapp.message.dto.MessageDto;
import com.bankapp.message.dto.MessageRequest;
import com.bankapp.messagerouter.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/messages/my")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<MessageDto> getMyMessages(
            @AuthenticationPrincipal Jwt jwt) {

        String username = jwt.getClaimAsString("preferred_username");

        return messageService.getMessagesForReceiver(username);
    }

    @GetMapping("/messages")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MessageDto> getAllMessages() {
        return messageService.getAllMessages();
    }

    @GetMapping("/messages/paged")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<MessageDto>> getMessagesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        return ResponseEntity.ok(
                messageService.getMessagesPaginated(
                        page,
                        size,
                        sortBy,
                        direction
                )
        );
    }

    @GetMapping("/message/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MessageDto> getMessageById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                messageService.getMessageById(id)
        );
    }

    @PostMapping("/message")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageDto> saveMessage(
            @Valid @RequestBody MessageRequest messageRequest) {

        return new ResponseEntity<>(
                messageService.saveMessage(messageRequest),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping("/message/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long id) {

        messageService.deleteMessage(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/message/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageDto> sendMessage(
            @Valid @RequestBody MessageRequest messageRequest) {

        log.info(
                "Received message request: sender={}, receiver={}",
                messageRequest.getSender(),
                messageRequest.getReceiver()
        );

        return new ResponseEntity<>(
                messageService.sendMessage(messageRequest),
                HttpStatus.CREATED
        );
    }
}
