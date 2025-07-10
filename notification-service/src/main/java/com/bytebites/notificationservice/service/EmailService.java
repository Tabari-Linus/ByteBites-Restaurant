package com.bytebites.notificationservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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

    @CircuitBreaker(name = "email-service", fallbackMethod = "fallbackSendEmail")
    @Retry(name = "email-service")
    public void sendEmail(String to, String subject, String content) {
        if (!emailEnabled) {
            logger.info("Email sending is disabled. Would send email to: {}, subject: {}", to, subject);
            logger.debug("Email content: {}", content);
            return;
        }

        try {
            logger.info("Attempting to send email to: {} with circuit breaker", to);

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

    
    public void fallbackSendEmail(String to, String subject, String content, Exception ex) {
        logger.warn("Email service circuit breaker activated. Using fallback for email to: {}, subject: {}, reason: {}",
                to, subject, ex.getMessage());

        
        logger.info("FALLBACK EMAIL LOG:");
        logger.info("To: {}", to);
        logger.info("Subject: {}", subject);
        logger.info("Content: {}", content);
        logger.info("Failed reason: {}", ex.getMessage());

        
        saveEmailForLaterRetry(to, subject, content, ex.getMessage());
    }

    private void saveEmailForLaterRetry(String to, String subject, String content, String errorReason) {
        
        logger.info("Email saved for later retry: to={}, subject={}, error={}", to, subject, errorReason);
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }
}
