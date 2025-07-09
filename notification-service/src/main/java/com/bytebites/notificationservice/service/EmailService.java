package com.bytebites.notificationservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String fromEmail;
    private final boolean emailEnabled;

    public EmailService(JavaMailSender mailSender,
                        @Value("${bytebites.email.from:noreply@bytebites.com}") String fromEmail,
                        @Value("${bytebites.email.enabled:false}") boolean emailEnabled) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
        this.emailEnabled = emailEnabled;
    }

    public void sendEmail(String to, String subject, String content) {
        if (!emailEnabled) {
            logger.info("Email sending is disabled. Would send email to: {}, subject: {}", to, subject);
            logger.debug("Email content: {}", content);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            logger.info("Email sent successfully to: {}, subject: {}", to, subject);

        } catch (Exception e) {
            logger.error("Failed to send email to: {}, subject: {}, error: {}", to, subject, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }
}
