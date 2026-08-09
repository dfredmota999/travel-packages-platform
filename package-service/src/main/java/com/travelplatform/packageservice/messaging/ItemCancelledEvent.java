package com.travelplatform.packageservice.messaging;

import java.util.UUID;

public record ItemCancelledEvent(UUID packageId, String reservationId) {
}
