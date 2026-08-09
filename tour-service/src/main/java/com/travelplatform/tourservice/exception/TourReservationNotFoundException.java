package com.travelplatform.tourservice.exception;

import java.util.UUID;

public class TourReservationNotFoundException extends RuntimeException {
    public TourReservationNotFoundException(UUID id) {
        super("Reserva de passeio não encontrada: " + id);
    }
}
