package com.travelplatform.flightservice.exception;

import java.util.UUID;

public class FlightOfferNotFoundException extends RuntimeException {
    public FlightOfferNotFoundException(UUID id) {
        super("Oferta de voo não encontrada: " + id);
    }
}
