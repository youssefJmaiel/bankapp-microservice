package com.bankapp.payment.service;

import com.bankapp.payment.converter.PaymentDtoConverter;
import com.bankapp.payment.domain.CreatePaymentRequest;
import com.bankapp.payment.domain.PaymentDto;
import com.bankapp.payment.entity.Payment;
import com.bankapp.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final String OWNER = "youssef";
    private static final String TOKEN = "test-access-token";
    private static final String ACCOUNT_URL = "http://account.test";

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentDtoConverter paymentDtoConverter;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    private RestTemplate restTemplate;
    private MockRestServiceServer accountServer;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        paymentService = new PaymentService(
                paymentRepository, paymentDtoConverter, restTemplateBuilder);

        ReflectionTestUtils.setField(
                paymentService, "accountServiceUrl", ACCOUNT_URL);

        accountServer = MockRestServiceServer.bindTo(restTemplate).build();

        lenient().when(paymentRepository.saveAndFlush(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        lenient().when(paymentDtoConverter.convert(any(Payment.class)))
                .thenAnswer(invocation -> toDto(invocation.getArgument(0)));
    }

    @Test
    void createPaymentShouldCompleteTransferAndForwardBearerToken() {
        CreatePaymentRequest request = validRequest();

        accountServer.expect(requestTo(ACCOUNT_URL + "/api/accounts/transfers"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer " + TOKEN))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"sourceAccountId\":1")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"destinationAccountId\":2")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"amount\":125.50")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("\"currency\":\"TND\"")))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        PaymentDto result = paymentService.createPayment(request, OWNER, TOKEN);

        accountServer.verify();
        assertNotNull(result);
        assertEquals(OWNER, result.getOwnerId());
        assertEquals(1L, result.getSourceAccountId());
        assertEquals(2L, result.getDestinationAccountId());
        assertEquals(new BigDecimal("125.50"), result.getAmount());
        assertEquals("TND", result.getCurrency());
        assertTrue(result.getReference().matches("PAY-[0-9a-fA-F-]{36}"));
        assertEquals("COMPLETED", result.getStatus());

        verify(paymentRepository, times(2)).saveAndFlush(any(Payment.class));
        verify(paymentRepository).existsByReference(result.getReference());
    }

    @Test
    void createPaymentShouldResolveDestinationIdFromAccountTransferResponse() {
        CreatePaymentRequest request = validRequest();
        request.setDestinationAccountId(null);
        request.setDestinationAccountNumber("  TN1234567890123456789012  ");

        accountServer.expect(requestTo(ACCOUNT_URL + "/api/accounts/transfers"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "\"destinationAccountNumber\":\"TN1234567890123456789012\"")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("\"destinationAccountId\""))))
                .andRespond(withSuccess(
                        "{\"destinationAccountId\":3}", MediaType.APPLICATION_JSON));

        PaymentDto result = paymentService.createPayment(request, OWNER, TOKEN);

        accountServer.verify();
        assertEquals(3L, result.getDestinationAccountId());
        assertEquals("COMPLETED", result.getStatus());
        verify(paymentRepository, times(2)).saveAndFlush(any(Payment.class));
    }

    @Test
    void createPaymentShouldRemainPendingWhenAccountResponseOmitsDestinationId() {
        CreatePaymentRequest request = validRequest();
        request.setDestinationAccountId(null);
        request.setDestinationAccountNumber("TN1234567890123456789012");

        accountServer.expect(requestTo(ACCOUNT_URL + "/api/accounts/transfers"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.createPayment(request, OWNER, TOKEN));

        accountServer.verify();
        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatus());

        verify(paymentRepository, times(1)).saveAndFlush(any(Payment.class));
        verify(paymentRepository).saveAndFlush(argThat(payment ->
                "PENDING".equals(payment.getStatus())
                        && payment.getDestinationAccountId() == null));
        verify(paymentDtoConverter, never()).convert(any(Payment.class));
    }

    @Test
    void createPaymentShouldRetryWhenGeneratedReferenceAlreadyExists() {
        CreatePaymentRequest request = validRequest();

        when(paymentRepository.existsByReference(anyString()))
                .thenReturn(true, false);

        accountServer.expect(requestTo(ACCOUNT_URL + "/api/accounts/transfers"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        PaymentDto result = paymentService.createPayment(request, OWNER, TOKEN);

        accountServer.verify();
        assertNotNull(result.getReference());
        verify(paymentRepository, times(2)).existsByReference(anyString());
    }

    @Test
    void createPaymentShouldRejectIdenticalSourceAndDestinationAccounts() {
        CreatePaymentRequest request = validRequest();
        request.setDestinationAccountId(request.getSourceAccountId());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.createPayment(request, OWNER, TOKEN));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verifyNoInteractions(paymentRepository, paymentDtoConverter);
        accountServer.verify();
    }

    @Test
    void createPaymentShouldMarkPaymentFailedWhenAccountRejectsTransfer() {
        accountServer.expect(requestTo(ACCOUNT_URL + "/api/accounts/transfers"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.createPayment(validRequest(), OWNER, TOKEN));

        accountServer.verify();
        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatus());
        assertTrue(exception.getReason().contains("HTTP 403"));

        verify(paymentRepository, times(2)).saveAndFlush(any(Payment.class));
        verify(paymentRepository, times(2)).saveAndFlush(argThat(
                payment -> "FAILED".equals(payment.getStatus())));
        verify(paymentDtoConverter, never()).convert(any(Payment.class));
    }

    @Test
    void createPaymentShouldMarkPaymentFailedWhenAccountIsUnreachable() throws IOException {
        accountServer.expect(requestTo(ACCOUNT_URL + "/api/accounts/transfers"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(request -> {
                    throw new IOException("Connection refused");
                });

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.createPayment(validRequest(), OWNER, TOKEN));

        accountServer.verify();
        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatus());
        assertEquals("Le service Account est injoignable.", exception.getReason());

        verify(paymentRepository, times(2)).saveAndFlush(any(Payment.class));
        verify(paymentRepository, times(2)).saveAndFlush(argThat(
                payment -> "FAILED".equals(payment.getStatus())));
        verify(paymentDtoConverter, never()).convert(any(Payment.class));
    }

    @Test
    void getPaymentByIdShouldReturnPaymentOwnedByCurrentUser() {
        Payment payment = payment(10L, OWNER);
        when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment));

        PaymentDto result = paymentService.getPaymentById(10L, OWNER);

        assertEquals(10L, result.getId());
        assertEquals(OWNER, result.getOwnerId());
        verify(paymentRepository).findById(10L);
        verify(paymentDtoConverter).convert(payment);
    }

    @Test
    void getPaymentByIdShouldReturnNotFoundWhenPaymentDoesNotExist() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.getPaymentById(99L, OWNER));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(paymentDtoConverter, never()).convert(any(Payment.class));
    }

    @Test
    void getPaymentByIdShouldHidePaymentOwnedByAnotherUser() {
        Payment payment = payment(11L, "another-user");
        when(paymentRepository.findById(11L)).thenReturn(Optional.of(payment));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paymentService.getPaymentById(11L, OWNER));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(paymentDtoConverter, never()).convert(any(Payment.class));
    }

    @Test
    void getMyPaymentsShouldReturnOnlyRepositoryResultsForOwner() {
        Payment first = payment(1L, OWNER);
        Payment second = payment(2L, OWNER);

        when(paymentRepository.findByOwnerIdOrderByCreatedAtDesc(OWNER))
                .thenReturn(List.of(first, second));

        List<PaymentDto> results = paymentService.getMyPayments(OWNER);

        assertEquals(2, results.size());
        assertEquals(1L, results.get(0).getId());
        assertEquals(2L, results.get(1).getId());
        assertTrue(results.stream().allMatch(dto -> OWNER.equals(dto.getOwnerId())));
        verify(paymentRepository).findByOwnerIdOrderByCreatedAtDesc(OWNER);
        verify(paymentDtoConverter, times(2)).convert(any(Payment.class));
    }

    @Test
    void getMyPaymentsShouldReturnEmptyListWhenOwnerHasNoPayments() {
        when(paymentRepository.findByOwnerIdOrderByCreatedAtDesc(OWNER))
                .thenReturn(List.of());

        List<PaymentDto> results = paymentService.getMyPayments(OWNER);

        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(paymentDtoConverter, never()).convert(any(Payment.class));
    }

    private CreatePaymentRequest validRequest() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setSourceAccountId(1L);
        request.setDestinationAccountId(2L);
        request.setAmount(new BigDecimal("125.50"));
        request.setCurrency("TND");
        return request;
    }

    private Payment payment(Long id, String ownerId) {
        Payment payment = new Payment();
        payment.setId(id);
        payment.setSourceAccountId(1L);
        payment.setDestinationAccountId(2L);
        payment.setOwnerId(ownerId);
        payment.setAmount(new BigDecimal("125.50"));
        payment.setCurrency("TND");
        payment.setReference("PAY-test-" + id);
        payment.setStatus("COMPLETED");
        return payment;
    }

    private PaymentDto toDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .sourceAccountId(payment.getSourceAccountId())
                .destinationAccountId(payment.getDestinationAccountId())
                .ownerId(payment.getOwnerId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .reference(payment.getReference())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
