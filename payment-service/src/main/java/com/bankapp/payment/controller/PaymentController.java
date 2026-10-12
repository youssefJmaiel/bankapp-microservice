package com.bankapp.payment.controller;

import com.bankapp.payment.domain.CreatePaymentRequest;
import com.bankapp.payment.domain.PaymentDto;
import com.bankapp.payment.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentDto> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Authentification requise");
        }
        String ownerId = getOwnerId(jwt);
        PaymentDto created = paymentService.createPayment(
                request, ownerId, jwt.getTokenValue());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> getPayment(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(
                paymentService.getPaymentById(id, getOwnerId(jwt))
        );
    }

    @GetMapping("/my")
    public ResponseEntity<List<PaymentDto>> getMyPayments(
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(
                paymentService.getMyPayments(getOwnerId(jwt))
        );
    }

    private String getOwnerId(Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");

        if (username != null && !username.trim().isEmpty()) {
            return username;
        }

        return jwt.getSubject();
    }
}
