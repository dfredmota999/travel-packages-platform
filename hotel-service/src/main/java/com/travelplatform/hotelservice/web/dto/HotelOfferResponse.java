package com.travelplatform.hotelservice.web.dto;

import com.travelplatform.hotelservice.domain.HotelOffer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record HotelOfferResponse(
        UUID id,
        String hotelName,
        String city,
        String roomType,
        LocalDate checkIn,
        LocalDate checkOut,
        BigDecimal pricePerNight,
        int totalRooms,
        int availableRooms
) {
    public static HotelOfferResponse from(HotelOffer offer) {
        return new HotelOfferResponse(
                offer.getId(), offer.getHotelName(), offer.getCity(), offer.getRoomType(),
                offer.getCheckIn(), offer.getCheckOut(), offer.getPricePerNight(),
                offer.getTotalRooms(), offer.getAvailableRooms());
    }
}
