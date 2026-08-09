package com.travelplatform.carreservationservice.domain;

import java.util.UUID;

public class InsufficientAvailabilityException extends RuntimeException {

    public InsufficientAvailabilityException(UUID offerId, int requested, int available) {
        super("Oferta " + offerId + " não tem veículos suficientes: solicitados=" + requested + ", disponíveis=" + available);
    }
}
