package com.bankapp.messagerouter.error;

public class RecipientDirectoryUnavailableException extends RuntimeException {

    public RecipientDirectoryUnavailableException() {
        super("Recipient directory is temporarily unavailable");
    }
}
