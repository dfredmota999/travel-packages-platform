package com.travelplatform.hotelservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate root próprio, referencia a oferta só pelo ID — mesma decisão de
 * desacoplamento do flight-service (ver FlightReservation).
 */
@Entity
@Table(name = "hotel_reservations")
public class HotelReservation {

    @Id
    private UUID id;

    private UUID offerId;
    private String customerId;
    private int guests;
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    protected HotelReservation() {
    }

    public HotelReservation(UUID offerId, String customerId, int guests) {
        this.id = UUID.randomUUID();
        this.offerId = offerId;
        this.customerId = customerId;
        this.guests = guests;
        this.status = ReservationStatus.CONFIRMED;
        this.createdAt = Instant.now();
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOfferId() {
        return offerId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public int getGuests() {
        return guests;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
