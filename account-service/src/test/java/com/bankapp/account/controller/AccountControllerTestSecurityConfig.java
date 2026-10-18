package com.bankapp.account.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
public class AccountControllerTestSecurityConfig {

    @Bean
    SecurityFilterChain accountControllerTestSecurityFilterChain(HttpSecurity http)
            throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .anyRequest().permitAll();
        return http.build();
    }
}
