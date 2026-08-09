package com.travelplatform.packageservice.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTOs de entrada da API. São mantidos separados do modelo de domínio
 * (TravelPackage e seus value objects) para não vazar detalhes de persistência
 * na camada web — o PackageMapper faz a tradução entre os dois mundos.
 */
public record CreatePackageRequest(
        @NotBlank String customerId,
        @Valid FlightRequest flight,
        @Valid HotelRequest hotel,
        @Valid CarRentalRequest carRental,
        @Valid TourRequest tour,
        @NotNull @Valid PaymentRequest paymentMethod
) {

    public record FlightRequest(
            @NotBlank String offerId,
            @NotBlank String origin,
            @NotBlank String destination,
            @NotNull LocalDate departureDate,
            @NotNull LocalDate returnDate,
            @Min(1) int passengers
    ) {
    }

    public record HotelRequest(
            @NotBlank String offerId,
            @NotNull LocalDate checkIn,
            @NotNull LocalDate checkOut,
            @NotBlank String roomType,
            @Min(1) int guests
    ) {
    }

    public record CarRentalRequest(
            @NotBlank String offerId,
            @NotNull LocalDate pickupDate,
            @NotNull LocalDate returnDate,
            @NotBlank String category
    ) {
    }

    public record TourRequest(
            @NotBlank String offerId,
            @NotNull LocalDate date,
            @Min(1) int participants
    ) {
    }

    public record PaymentRequest(
            @NotBlank String type,
            @Min(1) Integer installments
    ) {
    }
}
