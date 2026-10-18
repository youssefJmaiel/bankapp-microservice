package com.bankapp.payment.service;

import com.bankapp.payment.converter.PaymentDtoConverter;
import com.bankapp.payment.domain.CreatePaymentRequest;
import com.bankapp.payment.domain.PaymentDto;
import com.bankapp.payment.entity.Payment;
import com.bankapp.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentDtoConverter paymentDtoConverter;
    private final RestTemplate restTemplate;

    @Value("${bankapp.account-service.url:http://account-service:8089}")
    private String accountServiceUrl;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentDtoConverter paymentDtoConverter,
            RestTemplateBuilder restTemplateBuilder) {
        this.paymentRepository = paymentRepository;
        this.paymentDtoConverter = paymentDtoConverter;
        this.restTemplate = restTemplateBuilder.build();
    }

    public PaymentDto createPayment(
            CreatePaymentRequest request,
            String ownerId,
            String accessToken) {

        if (request.getSourceAccountId().equals(request.getDestinationAccountId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Les comptes source et destinataire doivent être différents");
        }

        String reference;
        do {
            reference = "PAY-" + java.util.UUID.randomUUID();
        } while (paymentRepository.existsByReference(reference));

        Payment payment = new Payment();
        payment.setSourceAccountId(request.getSourceAccountId());
        payment.setDestinationAccountId(request.getDestinationAccountId());
        payment.setOwnerId(ownerId);
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setReference(reference);
        payment.setStatus("PENDING");

        // Sauvegarde avant l'appel réseau : pas de transaction globale ici.
        Payment saved = paymentRepository.saveAndFlush(payment);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("sourceAccountId", request.getSourceAccountId());
            body.put("destinationAccountId", request.getDestinationAccountId());
            body.put("amount", request.getAmount());
            body.put("currency", request.getCurrency());
            body.put("reference", reference);

            restTemplate.postForEntity(
                    accountServiceUrl + "/api/accounts/transfers",
                    new HttpEntity<Map<String, Object>>(body, headers),
                    Object.class);

            saved.setStatus("COMPLETED");
            return paymentDtoConverter.convert(paymentRepository.saveAndFlush(saved));

        } catch (RestClientResponseException ex) {
            saved.setStatus("FAILED");
            paymentRepository.saveAndFlush(saved);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Account a refusé le transfert (HTTP "
                            + ex.getRawStatusCode() + "). Consultez les logs Account.",
                    ex);

        } catch (ResourceAccessException ex) {
            saved.setStatus("FAILED");
            paymentRepository.saveAndFlush(saved);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Le service Account est injoignable.",
                    ex);
        }
    }

    @Transactional(readOnly = true)
    public PaymentDto getPaymentById(Long id, String ownerId) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Paiement introuvable"
                ));

        if (!ownerId.equals(payment.getOwnerId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Paiement introuvable"
            );
        }

        return paymentDtoConverter.convert(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> getMyPayments(String ownerId) {
        return paymentRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId)
                .stream()
                .map(paymentDtoConverter::convert)
                .collect(Collectors.toList());
    }
}
