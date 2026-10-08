package com.bankapp.notification.service;

import com.bankapp.notification.dto.MailRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendMail(MailRequest request) {

        Context context = new Context();

        if (request.getVariables() != null) {
            context.setVariables(request.getVariables());
        } else {
            context.setVariables(Collections.emptyMap());
        }

        String htmlContent = templateEngine.process(
                request.getTemplate(),
                context
        );

        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    "UTF-8"
            );

            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new IllegalStateException(
                    "Unable to create notification email",
                    e
            );
        }
    }
}
