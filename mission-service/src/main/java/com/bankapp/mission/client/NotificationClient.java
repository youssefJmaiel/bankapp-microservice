package com.bankapp.mission.client;

import com.bankapp.notification.dto.MailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "bankapp-notification")
public interface NotificationClient {

    @PostMapping("/api/notifications/send")
    void sendNotification(@RequestBody MailRequest request);
}
