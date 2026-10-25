package com.bankapp.messagerouter.controller;

import com.bankapp.message.dto.MessageDto;
import com.bankapp.message.dto.MessageRequest;
import com.bankapp.messagerouter.service.MessageService;
import com.bankapp.messagerouter.client.RecipientDirectoryClient;
import org.springframework.security.access.AccessDeniedException;
import java.util.Collection;
import java.util.Map;
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
    private final RecipientDirectoryClient recipientDirectoryClient;

    public MessageController(
            MessageService messageService,
            RecipientDirectoryClient recipientDirectoryClient) {
        this.messageService = messageService;
        this.recipientDirectoryClient = recipientDirectoryClient;
    }

    @GetMapping("/messages/my")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<MessageDto> getMyMessages(
            @AuthenticationPrincipal Jwt jwt) {

        String username = jwt.getClaimAsString("preferred_username");

        return messageService.getMessagesForUser(username);
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
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        String username = authenticatedUsername(jwt);
        boolean isAdmin = hasExactlyOneMessagingRole(jwt, username).equals("ADMIN");
        return ResponseEntity.ok(
                messageService.getMessageByIdForUser(id, username, isAdmin)
        );
    }

    @PostMapping("/message")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageDto> saveMessage(
            @Valid @RequestBody MessageRequest messageRequest) {

        if (messageRequest.getSender() == null
                || messageRequest.getSender().trim().isEmpty()) {
            throw new IllegalArgumentException("Sender must not be blank");
        }

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
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MessageDto> sendMessage(
            @Valid @RequestBody MessageRequest messageRequest,
            @AuthenticationPrincipal Jwt jwt) {

        String username = authenticatedUsername(jwt);
        String role = hasExactlyOneMessagingRole(jwt, username);

        if (messageRequest.getReceiver() == null
                || messageRequest.getReceiver().trim().isEmpty()) {
            throw new IllegalArgumentException("Receiver must not be blank");
        }

        if (username.equals(messageRequest.getReceiver())) {
            throw new IllegalArgumentException("Cannot send a message to yourself");
        }

        List<String> recipients = recipientDirectoryClient.getEligibleRecipients(
                jwt.getTokenValue());
        if (!recipients.contains(messageRequest.getReceiver())) {
            throw new AccessDeniedException("Recipient is not eligible for this sender");
        }

        // L'expéditeur vient du JWT, jamais du corps envoyé par le frontend.
        messageRequest.setSender(username);
        log.info("Sending message: sender={}, receiver={}, role={}",
                username, messageRequest.getReceiver(), role);

        return new ResponseEntity<>(
                messageService.sendMessage(messageRequest),
                HttpStatus.CREATED
        );
    }

    private String authenticatedUsername(Jwt jwt) {
        if (jwt == null) {
            throw new AccessDeniedException("Authenticated username is missing");
        }
        String username = jwt.getClaimAsString("preferred_username");
        if (username == null || username.trim().isEmpty()) {
            throw new AccessDeniedException("Authenticated username is missing");
        }
        return username;
    }

    private String hasExactlyOneMessagingRole(Jwt jwt, String username) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        Object rolesObject = realmAccess == null ? null : realmAccess.get("roles");
        if (!(rolesObject instanceof Collection<?>)) {
            throw new AccessDeniedException("Realm roles are missing");
        }
        Collection<?> roles = (Collection<?>) rolesObject;
        boolean isUser = roles.contains("USER");
        boolean isAdmin = roles.contains("ADMIN");
        if (isUser == isAdmin) {
            throw new AccessDeniedException(
                    "Exactly one of USER or ADMIN must be assigned for messaging");
        }
        return isAdmin ? "ADMIN" : "USER";
    }
}
