package com.travelplatform.tourservice.web.dto;

import com.travelplatform.tourservice.domain.ReservationStatus;
import com.travelplatform.tourservice.domain.TourReservation;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

public record CreateReservationRequest(
        @NotBlank String customerId,
        @Min(1) int participants
) {

    public record Response(
            UUID id,
            UUID offerId,
            String customerId,
            int participants,
            ReservationStatus status,
            Instant createdAt
    ) {
        public static Response from(TourReservation reservation) {
            return new Response(
                    reservation.getId(), reservation.getOfferId(), reservation.getCustomerId(),
                    reservation.getParticipants(), reservation.getStatus(), reservation.getCreatedAt());
        }
    }
}
