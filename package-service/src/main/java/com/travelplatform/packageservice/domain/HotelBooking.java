package com.travelplatform.packageservice.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;

@Embeddable
public class HotelBooking {

    private String offerId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private String roomType;
    private int guests;
    private String reservationId;

    @Enumerated(EnumType.STRING)
    private BookingItemStatus status = BookingItemStatus.NOT_REQUESTED;

    protected HotelBooking() {
    }

    public HotelBooking(String offerId, LocalDate checkIn, LocalDate checkOut,
                         String roomType, int guests) {
        this.offerId = offerId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.roomType = roomType;
        this.guests = guests;
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

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public String getRoomType() {
        return roomType;
    }

    public int getGuests() {
        return guests;
    }

    public String getReservationId() {
        return reservationId;
    }

    public BookingItemStatus getStatus() {
        return status;
    }
}
