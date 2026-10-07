package com.bankapp.mission.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {

        if (SecurityContextHolder.getContext().getAuthentication()
                instanceof JwtAuthenticationToken) {

            JwtAuthenticationToken authentication =
                    (JwtAuthenticationToken) SecurityContextHolder
                            .getContext()
                            .getAuthentication();

            template.header(
                    "Authorization",
                    "Bearer " + authentication.getToken().getTokenValue()
            );
        }
    }
}
