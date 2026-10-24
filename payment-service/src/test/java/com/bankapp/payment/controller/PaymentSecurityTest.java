package com.bankapp.payment.controller;

import com.bankapp.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/payments/my"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(paymentService);
    }

    @Test
    void shouldRejectInvalidBearerToken() throws Exception {
        when(jwtDecoder.decode("invalid-token"))
                .thenThrow(new JwtException("Invalid token"));

        org.junit.jupiter.api.Assertions.assertThrows(
                AuthenticationServiceException.class,
                () -> mockMvc.perform(get("/api/payments/my")
                        .header("Authorization", "Bearer invalid-token"))
        );

        verifyNoInteractions(paymentService);
    }


    @Test
    void shouldRejectPaymentWhenSourceAccountIsMissing() throws Exception {
        assertInvalidPayment("""
                {
                  "destinationAccountId": 2,
                  "amount": 10.00,
                  "currency": "TND"
                }
                """);
    }

    @Test
    void shouldRejectPaymentWhenSourceAccountIdIsNegative() throws Exception {
        assertInvalidPayment("""
                {
                  "sourceAccountId": -1,
                  "destinationAccountId": 2,
                  "amount": 10.00,
                  "currency": "TND"
                }
                """);
    }

    @Test
    void shouldRejectPaymentWhenDestinationIsMissing() throws Exception {
        assertInvalidPayment("""
                {
                  "sourceAccountId": 1,
                  "amount": 10.00,
                  "currency": "TND"
                }
                """);
    }

    @Test
    void shouldRejectPaymentWhenAmountIsZero() throws Exception {
        assertInvalidPayment("""
                {
                  "sourceAccountId": 1,
                  "destinationAccountId": 2,
                  "amount": 0,
                  "currency": "TND"
                }
                """);
    }

    @Test
    void shouldRejectPaymentWhenAmountIsNegative() throws Exception {
        assertInvalidPayment("""
                {
                  "sourceAccountId": 1,
                  "destinationAccountId": 2,
                  "amount": -10.00,
                  "currency": "TND"
                }
                """);
    }

    @Test
    void shouldRejectPaymentWhenAmountHasMoreThanTwoDecimalPlaces() throws Exception {
        assertInvalidPayment("""
                {
                  "sourceAccountId": 1,
                  "destinationAccountId": 2,
                  "amount": 10.123,
                  "currency": "TND"
                }
                """);
    }

    @Test
    void shouldRejectPaymentWhenCurrencyIsNotThreeUppercaseLetters() throws Exception {
        assertInvalidPayment("""
                {
                  "sourceAccountId": 1,
                  "destinationAccountId": 2,
                  "amount": 10.00,
                  "currency": "tn"
                }
                """);
    }

    @Test
    void shouldRejectPaymentWhenDestinationAccountNumberIsMalformed() throws Exception {
        assertInvalidPayment("""
                {
                  "sourceAccountId": 1,
                  "destinationAccountNumber": "12345",
                  "amount": 10.00,
                  "currency": "TND"
                }
                """);
    }

    private void assertInvalidPayment(String json) throws Exception {
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/payments")
                                .with(jwt().jwt(token -> token
                                        .subject("subject-123")
                                        .claim("preferred_username", "alice")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(paymentService);
    }

    @Test
    void shouldAllowAuthenticatedJwtToReachController() throws Exception {
        mockMvc.perform(get("/api/payments/my")
                        .with(jwt().jwt(token -> token
                                .subject("subject-123")
                                .claim("preferred_username", "alice"))))
                .andExpect(status().isOk());
    }
}
