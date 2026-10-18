package com.bankapp.messagerouter.config;

import com.bankapp.messagerouter.error.MessageNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import javax.persistence.EntityNotFoundException;
import javax.validation.ValidationException;
import java.io.EOFException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleEOFException_shouldReturnBadRequest() {
        ResponseEntity<String> response =
                handler.handleEOFException(new EOFException("connection closed"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Client disconnected prematurely", response.getBody());
    }

    @Test
    void handleEOFException_shouldHandleNullMessage() {
        EOFException exception = new EOFException();

        ResponseEntity<String> response = handler.handleEOFException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Client disconnected prematurely", response.getBody());
    }

    @Test
    void handleMessageNotFound_shouldReturnNotFound() {
        ResponseEntity<String> response =
                handler.handleMessageNotFound(
                        new MessageNotFoundException("Message 42 not found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Message 42 not found", response.getBody());
    }

    @Test
    void handleMessageNotFound_shouldHandleNullMessage() {
        ResponseEntity<String> response =
                handler.handleMessageNotFound(new MessageNotFoundException());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("com.bankapp.messagerouter.error.MessageNotFoundException",
                response.getBody());
    }

    @Test
    void handleEntityNotFound_shouldReturnNotFoundBody() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleEntityNotFound(
                        new EntityNotFoundException("Entity missing"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Not Found", response.getBody().get("error"));
        assertEquals("Entity missing", response.getBody().get("message"));
    }

    @Test
    void handleBadRequest_shouldHandleIllegalArgumentException() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleBadRequest(new IllegalArgumentException("Invalid input"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Bad Request", response.getBody().get("error"));
        assertEquals("Invalid input", response.getBody().get("message"));
    }

    @Test
    void handleBadRequest_shouldHandleValidationException() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleBadRequest(new ValidationException("Validation failed"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Bad Request", response.getBody().get("error"));
        assertEquals("Validation failed", response.getBody().get("message"));
    }

    @Test
    void handleAccessDenied_shouldReturnForbidden() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleAccessDenied(new AccessDeniedException("Insufficient role"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Forbidden", response.getBody().get("error"));
        assertEquals("Access denied", response.getBody().get("message"));
    }

    @Test
    void handleGeneralException_shouldReturnInternalServerError() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleGeneralException(new IllegalStateException("Unexpected error"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal Server Error", response.getBody().get("error"));
        assertEquals("Unexpected error", response.getBody().get("message"));
    }

    @Test
    void handleGeneralException_shouldFallbackWhenMessageIsNull() {
        IllegalStateException exception = new IllegalStateException();

        ResponseEntity<Map<String, Object>> response =
                handler.handleGeneralException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(exception.toString(), response.getBody().get("message"));
    }

    @Test
    void handleMethodNotAllowed_shouldReturnMethodNotAllowed() {
        HttpRequestMethodNotSupportedException exception =
                new HttpRequestMethodNotSupportedException("PUT");

        ResponseEntity<Map<String, Object>> response =
                handler.handleMethodNotAllowed(exception);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Method Not Allowed", response.getBody().get("error"));
        assertEquals(exception.getMessage(), response.getBody().get("message"));
    }
}
