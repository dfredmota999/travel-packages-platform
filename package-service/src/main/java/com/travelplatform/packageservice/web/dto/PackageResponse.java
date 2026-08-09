package com.travelplatform.packageservice.web.dto;

import com.travelplatform.packageservice.domain.BookingItemStatus;
import com.travelplatform.packageservice.domain.PackageStatus;
import com.travelplatform.packageservice.domain.TravelPackage;
import java.time.Instant;
import java.util.UUID;

public record PackageResponse(
        UUID id,
        String customerId,
        PackageStatus status,
        Instant createdAt,
        Instant updatedAt,
        ItemStatus flight,
        ItemStatus hotel,
        ItemStatus carRental,
        ItemStatus tour,
        ItemStatus payment
) {

    public record ItemStatus(BookingItemStatus status, String reservationId) {
    }

    public static PackageResponse from(TravelPackage p) {
        return new PackageResponse(
                p.getId(),
                p.getCustomerId(),
                p.getStatus(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                new ItemStatus(p.getFlightBooking().getStatus(), p.getFlightBooking().getReservationId()),
                new ItemStatus(p.getHotelBooking().getStatus(), p.getHotelBooking().getReservationId()),
                new ItemStatus(p.getCarRentalBooking().getStatus(), p.getCarRentalBooking().getReservationId()),
                new ItemStatus(p.getTourBooking().getStatus(), p.getTourBooking().getReservationId()),
                new ItemStatus(p.getPaymentInfo() != null ? p.getPaymentInfo().getStatus() : BookingItemStatus.NOT_REQUESTED,
                        p.getPaymentInfo() != null ? p.getPaymentInfo().getTransactionId() : null)
        );
    }
}
