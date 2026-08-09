package com.travelplatform.packageservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tour_bookings")
public class TourBooking {

    @Id
    private UUID packageId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "package_id")
    private TravelPackage travelPackage;

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

    void attachTo(TravelPackage travelPackage) {
        this.travelPackage = travelPackage;
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
