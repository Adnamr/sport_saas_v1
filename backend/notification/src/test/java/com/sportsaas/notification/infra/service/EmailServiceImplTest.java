package com.sportsaas.notification.infra.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for EmailServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl(mailSender, templateEngine);
        ReflectionTestUtils.setField(emailService, "fromAddress", "noreply@sportsaas.com");
        ReflectionTestUtils.setField(emailService, "appName", "Sport SaaS");
    }

    @Test
    void shouldSendWelcomeEmail() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/welcome"), any(Context.class)))
            .thenReturn("<html>Welcome</html>");

        // When
        emailService.sendWelcomeEmail("user@example.com", "John", "https://verify.link");

        // Then
        verify(mailSender).send(mimeMessage);
        verify(templateEngine).process(eq("email/welcome"), any(Context.class));
    }

    @Test
    void shouldSendPasswordResetEmail() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/reset-password"), any(Context.class)))
            .thenReturn("<html>Reset</html>");

        // When
        emailService.sendPasswordResetEmail("user@example.com", "John", "https://reset.link");

        // Then
        verify(mailSender).send(mimeMessage);
        verify(templateEngine).process(eq("email/reset-password"), any(Context.class));
    }

    @Test
    void shouldSendOrderConfirmationEmail() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/order-confirmation"), any(Context.class)))
            .thenReturn("<html>Order</html>");

        // When
        emailService.sendOrderConfirmationEmail(
            "user@example.com", "John", "ORD-001", "2024-01-01", "100.00 EUR"
        );

        // Then
        verify(mailSender).send(mimeMessage);
        verify(templateEngine).process(eq("email/order-confirmation"), any(Context.class));
    }

    @Test
    void shouldSendInvoiceEmail() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/invoice"), any(Context.class)))
            .thenReturn("<html>Invoice</html>");

        // When
        emailService.sendInvoiceEmail(
            "user@example.com", "John", "INV-001", "2024-01-01", "100.00 EUR", "2024-01-15"
        );

        // Then
        verify(mailSender).send(mimeMessage);
        verify(templateEngine).process(eq("email/invoice"), any(Context.class));
    }

    @Test
    void shouldSendGenericEmail() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/custom"), any(Context.class)))
            .thenReturn("<html>Custom</html>");

        Map<String, Object> variables = new HashMap<>();
        variables.put("key", "value");

        // When
        emailService.sendEmail("user@example.com", "Subject", "custom", variables);

        // Then
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void shouldPassCorrectVariablesToTemplate() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        when(templateEngine.process(eq("email/welcome"), contextCaptor.capture()))
            .thenReturn("<html>Welcome</html>");

        // When
        emailService.sendWelcomeEmail("user@example.com", "John", "https://verify.link");

        // Then
        Context capturedContext = contextCaptor.getValue();
        assertThat(capturedContext.getVariable("firstName")).isEqualTo("John");
        assertThat(capturedContext.getVariable("verificationLink")).isEqualTo("https://verify.link");
        assertThat(capturedContext.getVariable("appName")).isEqualTo("Sport SaaS");
    }

    @Test
    void shouldThrowExceptionOnMailError() {
        // Given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/welcome"), any(Context.class)))
            .thenReturn("<html>Welcome</html>");
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(MimeMessage.class));

        // When/Then
        assertThatThrownBy(() ->
            emailService.sendWelcomeEmail("user@example.com", "John", "https://verify.link")
        ).isInstanceOf(RuntimeException.class);
    }
}
