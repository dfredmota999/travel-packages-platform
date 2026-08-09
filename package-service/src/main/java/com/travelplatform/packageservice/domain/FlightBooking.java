package com.travelplatform.packageservice.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;

/**
 * Value object: representa a reserva de voo dentro do agregado TravelPackage.
 * Não tem identidade própria fora do pacote — é comparado por valor, não por ID.
 * O "reservationId" é o identificador que o flight-service devolve quando confirma.
 */
@Embeddable
public class FlightBooking {

    private String offerId;
    private String origin;
    private String destination;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private int passengers;
    private String reservationId;

    @Enumerated(EnumType.STRING)
    private BookingItemStatus status = BookingItemStatus.NOT_REQUESTED;

    protected FlightBooking() {
        // exigido pelo JPA
    }

    public FlightBooking(String offerId, String origin, String destination,
                          LocalDate departureDate, LocalDate returnDate, int passengers) {
        this.offerId = offerId;
        this.origin = origin;
        this.destination = destination;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
        this.passengers = passengers;
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

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public int getPassengers() {
        return passengers;
    }

    public String getReservationId() {
        return reservationId;
    }

    public BookingItemStatus getStatus() {
        return status;
    }
}
