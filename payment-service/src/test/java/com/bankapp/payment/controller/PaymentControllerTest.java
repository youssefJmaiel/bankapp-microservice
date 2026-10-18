package com.bankapp.payment.controller;

import com.bankapp.payment.domain.CreatePaymentRequest;
import com.bankapp.payment.domain.PaymentDto;
import com.bankapp.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void createPaymentShouldReturnCreatedAndUseJwtUsername() {
        Jwt jwt = jwtWithUsername("alice");
        CreatePaymentRequest request = validRequest();
        PaymentDto expected = paymentDto(10L, "alice");

        when(paymentService.createPayment(request, "alice", "test-token"))
                .thenReturn(expected);

        ResponseEntity<PaymentDto> response =
                paymentController.createPayment(request, jwt);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(expected, response.getBody());
        verify(paymentService).createPayment(request, "alice", "test-token");
    }

    @Test
    void getPaymentShouldUseJwtUsernameAsOwner() {
        Jwt jwt = jwtWithUsername("alice");
        PaymentDto expected = paymentDto(10L, "alice");

        when(paymentService.getPaymentById(10L, "alice"))
                .thenReturn(expected);

        ResponseEntity<PaymentDto> response =
                paymentController.getPayment(10L, jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());
        verify(paymentService).getPaymentById(10L, "alice");
    }

    @Test
    void getMyPaymentsShouldReturnCurrentUsersPayments() {
        Jwt jwt = jwtWithUsername("alice");
        List<PaymentDto> expected = List.of(paymentDto(10L, "alice"));

        when(paymentService.getMyPayments("alice")).thenReturn(expected);

        ResponseEntity<List<PaymentDto>> response =
                paymentController.getMyPayments(jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());
        verify(paymentService).getMyPayments("alice");
    }

    @Test
    void shouldUseJwtSubjectWhenPreferredUsernameIsMissing() {
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("subject-123")
                .build();

        PaymentDto expected = paymentDto(10L, "subject-123");

        when(paymentService.getPaymentById(10L, "subject-123"))
                .thenReturn(expected);

        ResponseEntity<PaymentDto> response =
                paymentController.getPayment(10L, jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());
        verify(paymentService).getPaymentById(10L, "subject-123");
    }

    @Test
    void createPaymentShouldRejectMissingJwt() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paymentController.createPayment(validRequest(), null));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        verifyNoInteractions(paymentService);
    }

    private Jwt jwtWithUsername(String username) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("subject-123")
                .claim("preferred_username", username)
                .build();
    }

    private CreatePaymentRequest validRequest() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSourceAccountId(1L);
        request.setDestinationAccountId(2L);
        request.setAmount(new BigDecimal("125.50"));
        request.setCurrency("TND");
        return request;
    }

    private PaymentDto paymentDto(Long id, String owner) {
        return PaymentDto.builder()
                .id(id)
                .sourceAccountId(1L)
                .destinationAccountId(2L)
                .ownerId(owner)
                .amount(new BigDecimal("125.50"))
                .currency("TND")
                .reference("PAY-test-" + id)
                .status("COMPLETED")
                .build();
    }
}
