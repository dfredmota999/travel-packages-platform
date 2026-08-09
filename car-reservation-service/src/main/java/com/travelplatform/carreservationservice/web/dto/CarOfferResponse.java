package com.travelplatform.carreservationservice.web.dto;

import com.travelplatform.carreservationservice.domain.CarOffer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CarOfferResponse(
        UUID id,
        String category,
        String model,
        LocalDate pickupDate,
        LocalDate returnDate,
        BigDecimal dailyRate,
        int totalUnits,
        int availableUnits
) {
    public static CarOfferResponse from(CarOffer offer) {
        return new CarOfferResponse(
                offer.getId(), offer.getCategory(), offer.getModel(),
                offer.getPickupDate(), offer.getReturnDate(), offer.getDailyRate(),
                offer.getTotalUnits(), offer.getAvailableUnits());
    }
}
