package com.travelplatform.tourservice.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTourOfferRequest(
        @NotBlank String tourName,
        @NotBlank String location,
        @NotNull LocalDate date,
        @NotNull @DecimalMin("0.0") BigDecimal pricePerPerson,
        @Min(1) int totalSlots
) {
}
