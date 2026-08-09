package com.travelplatform.flightservice.messaging;

import java.util.UUID;

public record ItemCancelledEvent(UUID packageId, String reservationId) {
}
