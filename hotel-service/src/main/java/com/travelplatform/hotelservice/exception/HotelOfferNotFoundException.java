package com.travelplatform.hotelservice.exception;

import java.util.UUID;

public class HotelOfferNotFoundException extends RuntimeException {
    public HotelOfferNotFoundException(UUID id) {
        super("Oferta de hotel não encontrada: " + id);
    }
}
