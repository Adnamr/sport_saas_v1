package com.sportsaas.order.api.dto;

/**
 * Request DTO for cancelling an order.
 */
public record CancelOrderRequest(
    String reason
) {}
