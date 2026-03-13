package com.bankapp.messagerouter.error;

public class MessageNotFoundException extends RuntimeException {

    public MessageNotFoundException() {
        super();
    }

    public MessageNotFoundException(String message) {
        super(message);
    }
}