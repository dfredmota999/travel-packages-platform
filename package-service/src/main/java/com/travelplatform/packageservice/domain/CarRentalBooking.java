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
@Table(name = "car_rental_bookings")
public class CarRentalBooking {

    @Id
    private UUID packageId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "package_id")
    private TravelPackage travelPackage;

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
