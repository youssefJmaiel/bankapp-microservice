package com.bankapp.notification.controller;

import com.bankapp.notification.dto.MailRequest;
import com.bankapp.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<Void> sendMail(
            @Valid @RequestBody MailRequest request) {

        notificationService.sendMail(request);

        return ResponseEntity.ok().build();
    }
}
