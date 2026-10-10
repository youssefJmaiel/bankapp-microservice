package com.bankapp.hr.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    @Test
    void duplicateEmailReturnsConflict() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<Map<String, String>> response =
                handler.handleDuplicateEmail(
                        new DuplicateEmailException("test@example.com")
                );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(
                "Email already exists: test@example.com",
                response.getBody().get("error")
        );
    }
}
