package com.travelplatform.packageservice.messaging;

import java.util.UUID;

/** Comando publicado pelo package-service, consumido pelo serviço de produto dono do item. */
public record ReserveItemCommand(UUID packageId, String offerId, int quantity, String customerId) {
}
