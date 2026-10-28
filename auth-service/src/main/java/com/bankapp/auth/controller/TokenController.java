package com.bankapp.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/auth")
public class TokenController {

    private static final String TOKEN_URL = "http://host.docker.internal:8081/realms/spring-app/protocol/openid-connect/token";
    private static final String CLIENT_ID = "spring-boot-client";
    private static final String CLIENT_SECRET = System.getenv("KEYCLOAK_CLIENT_SECRET");

    @GetMapping("/token")
    public ResponseEntity<String> getToken(
            @RequestParam String username,
            @RequestParam String password) throws JsonProcessingException {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", CLIENT_ID);
        body.add("client_secret", CLIENT_SECRET);
        body.add("username", username);
        body.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(TOKEN_URL, request, String.class);

            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(response.getBody(), Object.class);
            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

            return ResponseEntity.status(response.getStatusCode())
                    .body(writer.writeValueAsString(json));

        } catch (HttpClientErrorException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body("{\"error\":\"authentication_failed\"}");

        } catch (ResourceAccessException ex) {
            return ResponseEntity.status(502)
                    .body("{\"error\":\"identity_provider_unavailable\"}");
        }
    }
}
