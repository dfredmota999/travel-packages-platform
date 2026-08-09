package com.travelplatform.carreservationservice.exception;

import java.util.UUID;

public class CarOfferNotFoundException extends RuntimeException {
    public CarOfferNotFoundException(UUID id) {
        super("Oferta de carro não encontrada: " + id);
    }
}
