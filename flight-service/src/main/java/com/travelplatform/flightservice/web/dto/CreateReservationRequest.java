package com.travelplatform.flightservice.web.dto;

import com.travelplatform.flightservice.domain.FlightReservation;
import com.travelplatform.flightservice.domain.ReservationStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

public record CreateReservationRequest(
        @NotBlank String customerId,
        @Min(1) int passengers
) {

    public record Response(
            UUID id,
            UUID offerId,
            String customerId,
            int passengers,
            ReservationStatus status,
            Instant createdAt
    ) {
        public static Response from(FlightReservation reservation) {
            return new Response(
                    reservation.getId(), reservation.getOfferId(), reservation.getCustomerId(),
                    reservation.getPassengers(), reservation.getStatus(), reservation.getCreatedAt());
        }
    }
}
