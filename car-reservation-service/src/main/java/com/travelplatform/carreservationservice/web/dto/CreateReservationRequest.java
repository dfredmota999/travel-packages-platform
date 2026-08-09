package com.travelplatform.carreservationservice.web.dto;

import com.travelplatform.carreservationservice.domain.CarReservation;
import com.travelplatform.carreservationservice.domain.ReservationStatus;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

public record CreateReservationRequest(
        @NotBlank String customerId
) {

    public record Response(
            UUID id,
            UUID offerId,
            String customerId,
            ReservationStatus status,
            Instant createdAt
    ) {
        public static Response from(CarReservation reservation) {
            return new Response(
                    reservation.getId(), reservation.getOfferId(), reservation.getCustomerId(),
                    reservation.getStatus(), reservation.getCreatedAt());
        }
    }
}
