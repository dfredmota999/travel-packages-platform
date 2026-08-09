package com.travelplatform.packageservice.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;

@Embeddable
public class TourBooking {

    private String offerId;
    private LocalDate date;
    private int participants;
    private String reservationId;

    @Enumerated(EnumType.STRING)
    private BookingItemStatus status = BookingItemStatus.NOT_REQUESTED;

    protected TourBooking() {
    }

    public TourBooking(String offerId, LocalDate date, int participants) {
        this.offerId = offerId;
        this.date = date;
        this.participants = participants;
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

    public LocalDate getDate() {
        return date;
    }

    public int getParticipants() {
        return participants;
    }

    public String getReservationId() {
        return reservationId;
    }

    public BookingItemStatus getStatus() {
        return status;
    }
}
