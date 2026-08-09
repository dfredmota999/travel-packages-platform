package com.travelplatform.packageservice.web.dto;

import com.travelplatform.packageservice.domain.BookingItemStatus;
import com.travelplatform.packageservice.domain.ItemType;
import com.travelplatform.packageservice.domain.PackageStatus;
import com.travelplatform.packageservice.domain.TravelPackage;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PackageResponse(
        UUID id,
        String customerId,
        PackageStatus status,
        Instant createdAt,
        Instant updatedAt,
        List<ItemResponse> items,
        ItemResponse payment
) {

    public record ItemResponse(ItemType itemType, String offerId, String reservationId, BookingItemStatus status) {
    }

    public static PackageResponse from(TravelPackage p) {
        List<ItemResponse> items = p.getItems().stream()
                .map(i -> new ItemResponse(i.getItemType(), i.getOfferId(), i.getReservationId(), i.getStatus()))
                .toList();

        ItemResponse payment = p.getPaymentInfo() == null ? null : new ItemResponse(
                null, null, p.getPaymentInfo().getTransactionId(), p.getPaymentInfo().getStatus());

        return new PackageResponse(p.getId(), p.getCustomerId(), p.getStatus(),
                p.getCreatedAt(), p.getUpdatedAt(), items, payment);
    }
}
