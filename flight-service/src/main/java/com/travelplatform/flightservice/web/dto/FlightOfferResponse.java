package com.travelplatform.flightservice.web.dto;

import com.travelplatform.flightservice.domain.FlightOffer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FlightOfferResponse(
        UUID id,
        String origin,
        String destination,
        LocalDate departureDate,
        LocalDate returnDate,
        String airline,
        BigDecimal price,
        int totalSeats,
        int availableSeats
) {
    public static FlightOfferResponse from(FlightOffer offer) {
        return new FlightOfferResponse(
                offer.getId(), offer.getOrigin(), offer.getDestination(),
                offer.getDepartureDate(), offer.getReturnDate(), offer.getAirline(),
                offer.getPrice(), offer.getTotalSeats(), offer.getAvailableSeats());
    }
}
