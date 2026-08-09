package com.travelplatform.tourservice.web.dto;

import com.travelplatform.tourservice.domain.TourOffer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TourOfferResponse(
        UUID id,
        String tourName,
        String location,
        LocalDate date,
        BigDecimal pricePerPerson,
        int totalSlots,
        int availableSlots
) {
    public static TourOfferResponse from(TourOffer offer) {
        return new TourOfferResponse(
                offer.getId(), offer.getTourName(), offer.getLocation(), offer.getDate(),
                offer.getPricePerPerson(), offer.getTotalSlots(), offer.getAvailableSlots());
    }
}
