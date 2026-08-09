package com.travelplatform.hotelservice.web.dto;

import com.travelplatform.hotelservice.domain.HotelReservation;
import com.travelplatform.hotelservice.domain.ReservationStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

public record CreateReservationRequest(
        @NotBlank String customerId,
        @Min(1) int guests
) {

    public record Response(
            UUID id,
            UUID offerId,
            String customerId,
            int guests,
            ReservationStatus status,
            Instant createdAt
    ) {
        public static Response from(HotelReservation reservation) {
            return new Response(
                    reservation.getId(), reservation.getOfferId(), reservation.getCustomerId(),
                    reservation.getGuests(), reservation.getStatus(), reservation.getCreatedAt());
        }
    }
}
