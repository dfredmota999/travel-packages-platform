package com.travelplatform.hotelservice.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateHotelOfferRequest(
        @NotBlank String hotelName,
        @NotBlank String city,
        @NotBlank String roomType,
        @NotNull LocalDate checkIn,
        @NotNull LocalDate checkOut,
        @NotNull @DecimalMin("0.0") BigDecimal pricePerNight,
        @Min(1) int totalRooms
) {
}
