package com.travelplatform.flightservice.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateFlightOfferRequest(
        @NotBlank String origin,
        @NotBlank String destination,
        @NotNull LocalDate departureDate,
        @NotNull LocalDate returnDate,
        @NotBlank String airline,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @Min(1) int totalSeats
) {
}
