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

/**
 * Entidade filha em tabela própria (flight_bookings), ligada ao TravelPackage
 * por chave primária compartilhada (@MapsId): o id desta tabela é o MESMO id
 * do pacote dono. Isso evita gerar/guardar uma FK redundante e, como bônus,
 * permite que o @OneToOne(fetch = LAZY) funcione de verdade — normalmente o
 * Hibernate ignora LAZY em @OneToOne comum (porque precisa de uma query extra
 * só pra saber se o relacionamento existe), mas com PK compartilhada ele já
 * sabe o id de antemão e consegue criar um proxy sem tocar o banco.
 */
@Entity
@Table(name = "flight_bookings")
public class FlightBooking {

    @Id
    private UUID packageId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "package_id")
    private TravelPackage travelPackage;

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

    /** Liga este item ao pacote dono — necessário para o @MapsId derivar o id compartilhado. */
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
