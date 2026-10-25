package com.bankapp.messagerouter.client;

import com.bankapp.messagerouter.error.RecipientDirectoryUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class RecipientDirectoryClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${HR_SERVICE_BASE_URL:http://localhost:8083}")
    private String hrServiceBaseUrl;

    public List<String> getEligibleRecipients(String accessToken) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new RecipientDirectoryUnavailableException();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        try {
            ResponseEntity<String[]> response = restTemplate.exchange(
                    hrServiceBaseUrl + "/api/employees/message-recipients",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String[].class
            );

            String[] body = response.getBody();
            return body == null ? Collections.emptyList() : Arrays.asList(body);
        } catch (RestClientException ex) {
            throw new RecipientDirectoryUnavailableException();
        }
    }
}
