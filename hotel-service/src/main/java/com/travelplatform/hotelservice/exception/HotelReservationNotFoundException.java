package com.travelplatform.hotelservice.exception;

import java.util.UUID;

public class HotelReservationNotFoundException extends RuntimeException {
    public HotelReservationNotFoundException(UUID id) {
        super("Reserva de hotel não encontrada: " + id);
    }
}
