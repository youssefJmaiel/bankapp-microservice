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
    void shouldAllowAuthenticatedJwtToReachController() throws Exception {
        mockMvc.perform(get("/api/payments/my")
                        .with(jwt().jwt(token -> token
                                .subject("subject-123")
                                .claim("preferred_username", "alice"))))
                .andExpect(status().isOk());
    }
}
