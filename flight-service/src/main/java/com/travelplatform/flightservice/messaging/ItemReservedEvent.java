package com.travelplatform.flightservice.messaging;

import java.util.UUID;

public record ItemReservedEvent(UUID packageId, String offerId, String reservationId) {
}
