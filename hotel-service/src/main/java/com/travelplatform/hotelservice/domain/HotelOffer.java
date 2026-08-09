package com.travelplatform.hotelservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Aggregate root do bounded context "Hotel". Representa uma oferta de quarto
 * no catálogo (um tipo de quarto, num período, com N unidades disponíveis) —
 * não uma reserva específica. @Version implementa lock otimista, mesma ideia
 * do flight-service: evita "vender" o mesmo quarto duas vezes em reservas
 * concorrentes.
 */
@Entity
@Table(name = "hotel_offers")
public class HotelOffer {

    @Id
    private UUID id;

    private String hotelName;
    private String city;
    private String roomType;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private BigDecimal pricePerNight;
    private int totalRooms;
    private int availableRooms;

    @Version
    private Long version;

    protected HotelOffer() {
        // exigido pelo JPA
    }

    public HotelOffer(String hotelName, String city, String roomType, LocalDate checkIn,
                       LocalDate checkOut, BigDecimal pricePerNight, int totalRooms) {
        if (totalRooms <= 0) {
            throw new IllegalArgumentException("totalRooms deve ser maior que zero");
        }
        this.id = UUID.randomUUID();
        this.hotelName = hotelName;
        this.city = city;
        this.roomType = roomType;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.pricePerNight = pricePerNight;
        this.totalRooms = totalRooms;
        this.availableRooms = totalRooms;
    }

    public void reserveRooms(int rooms) {
        if (rooms <= 0) {
            throw new IllegalArgumentException("rooms deve ser maior que zero");
        }
        if (rooms > availableRooms) {
            throw new InsufficientAvailabilityException(this.id, rooms, availableRooms);
        }
        this.availableRooms -= rooms;
    }

    public void releaseRooms(int rooms) {
        this.availableRooms = Math.min(totalRooms, this.availableRooms + rooms);
    }

    public UUID getId() {
        return id;
    }

    public String getHotelName() {
        return hotelName;
    }

    public String getCity() {
        return city;
    }

    public String getRoomType() {
        return roomType;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public int getTotalRooms() {
        return totalRooms;
    }

    public int getAvailableRooms() {
        return availableRooms;
    }

    public Long getVersion() {
        return version;
    }
}
