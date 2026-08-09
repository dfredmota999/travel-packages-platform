package com.travelplatform.flightservice.messaging;

import java.util.UUID;

public record ReserveItemCommand(UUID packageId, String offerId, int quantity, String customerId) {
}
