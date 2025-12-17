package com.sportsaas.notification.domain.service;

import java.util.Map;
import java.util.UUID;

/**
 * Email service interface for sending notifications.
 */
public interface EmailService {

    /**
     * Send a welcome email to a new user.
     */
    void sendWelcomeEmail(String to, String firstName, String verificationLink);

    /**
     * Send a password reset email.
     */
    void sendPasswordResetEmail(String to, String firstName, String resetLink);

    /**
     * Send an order confirmation email.
     */
    void sendOrderConfirmationEmail(String to, String firstName, String orderNumber,
                                     String orderDate, String totalAmount);

    /**
     * Send an invoice email.
     */
    void sendInvoiceEmail(String to, String firstName, String invoiceNumber,
                          String invoiceDate, String totalAmount, String dueDate);

    /**
     * Send a generic email with a custom template.
     */
    void sendEmail(String to, String subject, String template, Map<String, Object> variables);
}
