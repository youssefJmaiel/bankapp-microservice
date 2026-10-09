package com.bankapp.hr.service;

import com.bankapp.hr.entity.Employee;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class KeycloakAdminService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${keycloak.admin.base-url}")
    private String baseUrl;

    @Value("${keycloak.admin.realm:spring-app}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    public void createAndActivateEmployee(Employee employee) {
        String userId = null;

        try {
            String token = obtainAdminToken();

            Map<String, Object> user = new HashMap<>();
            user.put("username", employee.getEmail());
            user.put("email", employee.getEmail());
            user.put("firstName", employee.getFirstName());
            user.put("lastName", employee.getLastName());
            user.put("enabled", true);
            user.put("emailVerified", false);

            HttpHeaders headers = bearerHeaders(token);
            HttpEntity<Map<String, Object>> createRequest =
                    new HttpEntity<>(user, headers);

            URI createUri = UriComponentsBuilder
                    .fromHttpUrl(baseUrl)
                    .pathSegment("admin", "realms", realm, "users")
                    .build()
                    .encode()
                    .toUri();

            ResponseEntity<Void> createResponse = restTemplate.exchange(
                    createUri,
                    HttpMethod.POST,
                    createRequest,
                    Void.class
            );

            URI location = createResponse.getHeaders().getLocation();
            if (location == null || location.getPath() == null) {
                throw new IllegalStateException(
                        "Keycloak a créé le compte sans retourner son identifiant."
                );
            }

            String path = location.getPath();
            userId = path.substring(path.lastIndexOf('/') + 1);

            assignUserRole(token, userId);
            sendActivationEmail(token, userId);

        } catch (RuntimeException exception) {
            if (userId != null) {
                try {
                    deleteKeycloakUser(obtainAdminToken(), userId);
                } catch (RuntimeException cleanupException) {
                    exception.addSuppressed(cleanupException);
                }
            }

            throw new IllegalStateException(
                    "Impossible de créer et d'activer le compte Keycloak de l'employé.",
                    exception
            );
        }
    }

    private String obtainAdminToken() {
        String tokenUrl = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .pathSegment("realms", realm, "protocol", "openid-connect", "token")
                .build()
                .encode()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                tokenUrl,
                new HttpEntity<>(form, headers),
                Map.class
        );

        Map body = response.getBody();
        Object accessToken = body == null ? null : body.get("access_token");

        if (!(accessToken instanceof String) || ((String) accessToken).isBlank()) {
            throw new IllegalStateException(
                    "Keycloak n'a pas retourné de jeton d'administration."
            );
        }

        return (String) accessToken;
    }

    private void assignUserRole(String token, String userId) {
        URI roleUri = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .pathSegment("admin", "realms", realm, "roles", "USER")
                .build()
                .encode()
                .toUri();

        ResponseEntity<Map> roleResponse = restTemplate.exchange(
                roleUri,
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(token)),
                Map.class
        );

        Map role = roleResponse.getBody();
        if (role == null || role.get("id") == null) {
            throw new IllegalStateException(
                    "Le rôle USER est introuvable dans Keycloak."
            );
        }

        URI mappingUri = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .pathSegment(
                        "admin", "realms", realm, "users", userId,
                        "role-mappings", "realm"
                )
                .build()
                .encode()
                .toUri();

        restTemplate.exchange(
                mappingUri,
                HttpMethod.POST,
                new HttpEntity<>(Collections.singletonList(role), bearerHeaders(token)),
                Void.class
        );
    }

    private void sendActivationEmail(String token, String userId) {
        URI activationUri = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .pathSegment(
                        "admin", "realms", realm, "users", userId,
                        "execute-actions-email"
                )
                .queryParam("lifespan", 86400)
                .build()
                .encode()
                .toUri();

        restTemplate.exchange(
                activationUri,
                HttpMethod.PUT,
                new HttpEntity<>(
                        Arrays.asList("VERIFY_EMAIL", "UPDATE_PASSWORD"),
                        bearerHeaders(token)
                ),
                Void.class
        );
    }

    private void deleteKeycloakUser(String token, String userId) {
        URI deleteUri = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .pathSegment("admin", "realms", realm, "users", userId)
                .build()
                .encode()
                .toUri();

        restTemplate.exchange(
                deleteUri,
                HttpMethod.DELETE,
                new HttpEntity<>(bearerHeaders(token)),
                Void.class
        );
    }

    private HttpHeaders bearerHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
