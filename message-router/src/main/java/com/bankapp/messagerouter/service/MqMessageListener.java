package com.bankapp.messagerouter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqMessageListener {

    public void handleMessage(String message) {
        log.info("Received message from MQ: {}", message);
    }
}