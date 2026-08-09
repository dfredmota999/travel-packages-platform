package com.travelplatform.carreservationservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "car_reservations")
public class CarReservation {

    @Id
    private UUID id;

    private UUID offerId;
    private String customerId;
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    protected CarReservation() {
    }

    public CarReservation(UUID offerId, String customerId) {
        this.id = UUID.randomUUID();
        this.offerId = offerId;
        this.customerId = customerId;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
