package com.bankapp.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;


@Configuration
public class OAuth2ClientConfig {

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        // Manager correct pour client_credentials
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository,
                        authorizedClientService);

        // Permet d'utiliser le client_credentials grant
        authorizedClientManager.setAuthorizedClientProvider(
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build());

        return authorizedClientManager;
    }

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }
}