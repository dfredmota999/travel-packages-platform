package com.travelplatform.flightservice.domain;

import java.util.UUID;

public class InsufficientAvailabilityException extends RuntimeException {

    public InsufficientAvailabilityException(UUID offerId, int requested, int available) {
        super("Oferta " + offerId + " não tem assentos suficientes: solicitados=" + requested + ", disponíveis=" + available);
    }
}
