package com.travelplatform.hotelservice.domain;

import java.util.UUID;

public class InsufficientAvailabilityException extends RuntimeException {

    public InsufficientAvailabilityException(UUID offerId, int requested, int available) {
        super("Oferta " + offerId + " não tem quartos suficientes: solicitados=" + requested + ", disponíveis=" + available);
    }
}
