package com.travelplatform.tourservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tour_reservations")
public class TourReservation {

    @Id
    private UUID id;

    private UUID offerId;
    private String customerId;
    private int participants;
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    protected TourReservation() {
    }

    public TourReservation(UUID offerId, String customerId, int participants) {
        this.id = UUID.randomUUID();
        this.offerId = offerId;
        this.customerId = customerId;
        this.participants = participants;
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

    public int getParticipants() {
        return participants;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
