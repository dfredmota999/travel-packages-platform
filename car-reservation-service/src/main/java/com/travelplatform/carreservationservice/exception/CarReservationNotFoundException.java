package com.travelplatform.carreservationservice.exception;

import java.util.UUID;

public class CarReservationNotFoundException extends RuntimeException {
    public CarReservationNotFoundException(UUID id) {
        super("Reserva de carro não encontrada: " + id);
    }
}
