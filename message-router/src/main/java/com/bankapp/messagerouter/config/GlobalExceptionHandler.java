package com.bankapp.messagerouter.config;

import com.bankapp.messagerouter.error.MessageNotFoundException;
import com.bankapp.messagerouter.error.PartnerNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.persistence.EntityNotFoundException;
import javax.validation.ValidationException;
import java.io.EOFException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private String getExceptionMessage(Exception ex) {
        return ex.getMessage() != null ? ex.getMessage() : ex.toString();
    }

    @ExceptionHandler(EOFException.class)
    public ResponseEntity<String> handleEOFException(EOFException e) {
        log.warn("Client disconnected prematurely: {}", getExceptionMessage(e));
        return new ResponseEntity<>("Client disconnected prematurely", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PartnerNotFoundException.class)
    public ResponseEntity<String> handlePartnerNotFound(PartnerNotFoundException ex) {
        log.warn("Partner not found: {}", getExceptionMessage(ex));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(getExceptionMessage(ex));
    }

    @ExceptionHandler(MessageNotFoundException.class)
    public ResponseEntity<String> handleMessageNotFound(MessageNotFoundException ex) {
        log.warn("Message not found: {}", getExceptionMessage(ex));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(getExceptionMessage(ex));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", getExceptionMessage(ex));
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Not Found");
        body.put("message", getExceptionMessage(ex));
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({IllegalArgumentException.class, ValidationException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex) {
        log.warn("Bad request: {}", getExceptionMessage(ex));
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Bad Request");
        body.put("message", getExceptionMessage(ex));
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

//    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
//    public ResponseEntity<Map<String, Object>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
//        log.warn("Method not allowed: {}", getExceptionMessage(ex));
//        Map<String, Object> body = new HashMap<>();
//        body.put("error", "Method Not Allowed");
//        body.put("message", getExceptionMessage(ex));
//        return new ResponseEntity<>(body, HttpStatus.METHOD_NOT_ALLOWED);
//    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        log.error("An unexpected error occurred: {}", getExceptionMessage(ex), ex);
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Internal Server Error");
        body.put("message", getExceptionMessage(ex));
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(org.springframework.web.server.MethodNotAllowedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotAllowed(org.springframework.web.server.MethodNotAllowedException ex) {
        log.warn("Method not allowed: {}", getExceptionMessage(ex));
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Method Not Allowed");
        body.put("message", getExceptionMessage(ex));
        return new ResponseEntity<>(body, HttpStatus.METHOD_NOT_ALLOWED);
    }
}