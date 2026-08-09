package com.travelplatform.tourservice.domain;

import java.util.UUID;

public class InsufficientAvailabilityException extends RuntimeException {

    public InsufficientAvailabilityException(UUID offerId, int requested, int available) {
        super("Oferta " + offerId + " não tem vagas suficientes: solicitadas=" + requested + ", disponíveis=" + available);
    }
}
