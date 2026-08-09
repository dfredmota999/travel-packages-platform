package com.travelplatform.carreservationservice.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCarOfferRequest(
        @NotBlank String category,
        @NotBlank String model,
        @NotNull LocalDate pickupDate,
        @NotNull LocalDate returnDate,
        @NotNull @DecimalMin("0.0") BigDecimal dailyRate,
        @Min(1) int totalUnits
) {
}
