package com.sportsaas.order.api.dto;

import java.time.LocalDate;

/**
 * Request DTO for returning a rental.
 */
public record ReturnRentalRequest(
    LocalDate actualReturnDate
) {}
