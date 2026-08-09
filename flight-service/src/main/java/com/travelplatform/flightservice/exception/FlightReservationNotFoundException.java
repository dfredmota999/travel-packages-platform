package com.travelplatform.flightservice.exception;

import java.util.UUID;

public class FlightReservationNotFoundException extends RuntimeException {
    public FlightReservationNotFoundException(UUID id) {
        super("Reserva de voo não encontrada: " + id);
    }
}
