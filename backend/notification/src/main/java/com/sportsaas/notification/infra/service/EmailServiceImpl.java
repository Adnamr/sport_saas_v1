package com.sportsaas.notification.infra.service;

import com.sportsaas.notification.domain.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of EmailService with async sending and retry support.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.from:noreply@sportsaas.com}")
    private String fromAddress;

    @Value("${app.name:Sport SaaS}")
    private String appName;

    @Override
    @Async
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void sendWelcomeEmail(String to, String firstName, String verificationLink) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", firstName);
        variables.put("verificationLink", verificationLink);
        variables.put("appName", appName);

        sendEmail(to, "Bienvenue sur " + appName, "welcome", variables);
        log.info("Welcome email sent to: {}", to);
    }

    @Override
    @Async
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void sendPasswordResetEmail(String to, String firstName, String resetLink) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", firstName);
        variables.put("resetLink", resetLink);
        variables.put("appName", appName);

        sendEmail(to, "Réinitialisation de votre mot de passe - " + appName, "reset-password", variables);
        log.info("Password reset email sent to: {}", to);
    }

    @Override
    @Async
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void sendOrderConfirmationEmail(String to, String firstName, String orderNumber,
                                            String orderDate, String totalAmount) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", firstName);
        variables.put("orderNumber", orderNumber);
        variables.put("orderDate", orderDate);
        variables.put("totalAmount", totalAmount);
        variables.put("appName", appName);

        sendEmail(to, "Confirmation de commande #" + orderNumber + " - " + appName,
                  "order-confirmation", variables);
        log.info("Order confirmation email sent to: {} for order: {}", to, orderNumber);
    }

    @Override
    @Async
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void sendInvoiceEmail(String to, String firstName, String invoiceNumber,
                                  String invoiceDate, String totalAmount, String dueDate) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", firstName);
        variables.put("invoiceNumber", invoiceNumber);
        variables.put("invoiceDate", invoiceDate);
        variables.put("totalAmount", totalAmount);
        variables.put("dueDate", dueDate);
        variables.put("appName", appName);

        sendEmail(to, "Facture #" + invoiceNumber + " - " + appName, "invoice", variables);
        log.info("Invoice email sent to: {} for invoice: {}", to, invoiceNumber);
    }

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void sendEmail(String to, String subject, String template, Map<String, Object> variables) {
        try {
            Context context = new Context();
            context.setVariables(variables);

            String htmlContent = templateEngine.process("email/" + template, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.debug("Email sent successfully to: {} with template: {}", to, template);

        } catch (MessagingException e) {
            log.error("Failed to send email to: {} with template: {}", to, template, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
