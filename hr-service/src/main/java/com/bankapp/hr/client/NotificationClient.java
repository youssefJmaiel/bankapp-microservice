package com.bankapp.hr.client;

import com.bankapp.notification.dto.MailRequest;
import com.bankapp.hr.config.FeignAuthConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@FeignClient(name = "bankapp-notification", configuration = FeignAuthConfig.class)
public interface NotificationClient {

    @PostMapping("/api/notifications/send")
    void sendMail(@Valid @RequestBody MailRequest request);
}
