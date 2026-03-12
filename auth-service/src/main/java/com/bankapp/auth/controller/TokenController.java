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
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/auth")
public class TokenController {

    private static final String TOKEN_URL = "http://localhost:8081/realms/spring-app/protocol/openid-connect/token";
    private static final String CLIENT_ID = "spring-boot-client";
    private static final String CLIENT_SECRET = "ASXwzdPnBexUvwE1B7dUal05QOXiOP9L";

    @GetMapping("/token")
    public String getToken(@RequestParam String username, @RequestParam String password) throws JsonProcessingException {

        RestTemplate restTemplate = new RestTemplate();

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Body
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", CLIENT_ID);
        body.add("client_secret", CLIENT_SECRET);
        body.add("username", username);   // CORRECT ici
        body.add("password", password);   // CORRECT ici

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        // Appel Keycloak
        ResponseEntity<String> response = restTemplate.postForEntity(TOKEN_URL, request, String.class);

        // Convertir le JSON en joli format
        ObjectMapper mapper = new ObjectMapper();
        Object json = mapper.readValue(response.getBody(), Object.class);
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        return writer.writeValueAsString(json);
    }
}