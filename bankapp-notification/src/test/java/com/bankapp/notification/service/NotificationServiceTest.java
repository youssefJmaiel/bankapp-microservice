package com.bankapp.notification.service;

import com.bankapp.notification.dto.MailRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.mail.Session;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    private JavaMailSender mailSender;
    private TemplateEngine templateEngine;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);

        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode(TemplateMode.HTML);

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        templateEngine = engine;

        when(mailSender.createMimeMessage()).thenAnswer(invocation ->
                new MimeMessage(Session.getInstance(new Properties())));

        notificationService = new NotificationService(mailSender, templateEngine);
    }

    @Test
    void sendMailShouldRenderTemplateAndSendHtmlMessage() throws Exception {
        MailRequest request = MailRequest.builder()
                .to("recipient@example.com")
                .subject("Test notification")
                .template("<html><body>Notification de test</body></html>")
                .build();

        notificationService.sendMail(request);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendMailShouldAcceptNullVariables() throws Exception {
        MailRequest request = MailRequest.builder()
                .to("recipient@example.com")
                .subject("Test notification")
                .template("<p>Notification sans variables</p>")
                .variables(null)
                .build();

        assertDoesNotThrow(() -> notificationService.sendMail(request));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendMailShouldRenderProvidedVariables() throws Exception {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", "Youssef");
        variables.put("missionTitle", "Migration bancaire");

        MailRequest request = MailRequest.builder()
                .to("recipient@example.com")
                .subject("Mission assignée")
                .template("<p th:text=\"${firstName + ' - ' + missionTitle}\">Mission</p>")
                .variables(variables)
                .build();

        assertDoesNotThrow(() -> notificationService.sendMail(request));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendMailShouldWrapMessagingExceptionInIllegalStateException() throws Exception {
        MimeMessage failingMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(failingMessage);
        doThrow(new MessagingException("Simulated subject error"))
                .when(failingMessage)
                .setSubject(anyString(), anyString());

        MailRequest request = MailRequest.builder()
                .to("recipient@example.com")
                .subject("Test notification")
                .template("<p>Test</p>")
                .build();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> notificationService.sendMail(request)
        );

        assertEquals("Unable to create notification email", exception.getMessage());
        assertInstanceOf(MessagingException.class, exception.getCause());
        assertEquals("Simulated subject error", exception.getCause().getMessage());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
