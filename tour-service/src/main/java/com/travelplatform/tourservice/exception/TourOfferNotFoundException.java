package com.travelplatform.tourservice.exception;

import java.util.UUID;

public class TourOfferNotFoundException extends RuntimeException {
    public TourOfferNotFoundException(UUID id) {
        super("Oferta de passeio não encontrada: " + id);
    }
}
