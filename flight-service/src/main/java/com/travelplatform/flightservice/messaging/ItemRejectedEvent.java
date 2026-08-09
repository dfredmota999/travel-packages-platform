package com.travelplatform.flightservice.messaging;

import java.util.UUID;

public record ItemRejectedEvent(UUID packageId, String offerId, String reason) {
}
