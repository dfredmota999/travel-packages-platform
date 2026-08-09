package com.travelplatform.packageservice.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;

@Embeddable
public class CarRentalBooking {

    private String offerId;
    private LocalDate pickupDate;
    private LocalDate returnDate;
    private String category;
    private String reservationId;

    @Enumerated(EnumType.STRING)
    private BookingItemStatus status = BookingItemStatus.NOT_REQUESTED;

    protected CarRentalBooking() {
    }

    public CarRentalBooking(String offerId, LocalDate pickupDate, LocalDate returnDate, String category) {
        this.offerId = offerId;
        this.pickupDate = pickupDate;
        this.returnDate = returnDate;
        this.category = category;
        this.status = BookingItemStatus.PENDING;
    }

    public void confirm(String reservationId) {
        this.reservationId = reservationId;
        this.status = BookingItemStatus.CONFIRMED;
    }

    public void reject() {
        this.status = BookingItemStatus.REJECTED;
    }

    public void cancel() {
        this.status = BookingItemStatus.CANCELLED;
    }

    public boolean isRequested() {
        return status != BookingItemStatus.NOT_REQUESTED;
    }

    public String getOfferId() {
        return offerId;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public String getCategory() {
        return category;
    }

    public String getReservationId() {
        return reservationId;
    }

    public BookingItemStatus getStatus() {
        return status;
    }
}
