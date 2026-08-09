package com.travelplatform.flightservice.messaging;

import java.util.UUID;

public record CancelItemCommand(UUID packageId, String reservationId) {
}
