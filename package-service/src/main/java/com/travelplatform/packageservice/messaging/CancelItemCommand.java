package com.travelplatform.packageservice.messaging;

import java.util.UUID;

/** Comando de compensação — cancela uma reserva já confirmada. */
public record CancelItemCommand(UUID packageId, String reservationId) {
}
