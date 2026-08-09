package com.travelplatform.flightservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate root próprio (não é um "child" de FlightOffer). Referencia a oferta
 * apenas pelo ID (offerId) — decisão de DDD pra manter os dois agregados
 * fracamente acoplados, mesmo estando na mesma base de dados/serviço.
 * A consistência entre "debitar assento" e "criar reserva" é garantida na
 * camada de aplicação (FlightOfferService), numa única transação.
 */
@Entity
@Table(name = "flight_reservations")
public class FlightReservation {

    @Id
    private UUID id;

    private UUID offerId;
    private String customerId;
    private int passengers;
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    protected FlightReservation() {
    }

    public FlightReservation(UUID offerId, String customerId, int passengers) {
        this.id = UUID.randomUUID();
        this.offerId = offerId;
        this.customerId = customerId;
        this.passengers = passengers;
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

    public int getPassengers() {
        return passengers;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
