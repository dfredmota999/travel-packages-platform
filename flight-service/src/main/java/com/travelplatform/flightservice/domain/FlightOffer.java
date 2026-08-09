package com.travelplatform.flightservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Aggregate root do bounded context "Voo". Representa uma oferta no catálogo
 * (não uma reserva específica). O campo @Version implementa lock otimista:
 * se dois clientes tentarem reservar o(s) último(s) assento(s) ao mesmo tempo,
 * o Hibernate detecta o conflito de versão e um dos dois recebe
 * OptimisticLockException — em vez de "vender" o mesmo assento duas vezes.
 * (alternativa a isso seria lock pessimista com SELECT ... FOR UPDATE, mais
 * caro em throughput; bom tópico pra discutir trade-offs em entrevista)
 */
@Entity
@Table(name = "flight_offers")
public class FlightOffer {

    @Id
    private UUID id;

    private String origin;
    private String destination;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private String airline;
    private BigDecimal price;
    private int totalSeats;
    private int availableSeats;

    @Version
    private Long version;

    protected FlightOffer() {
        // exigido pelo JPA
    }

    public FlightOffer(String origin, String destination, LocalDate departureDate,
                        LocalDate returnDate, String airline, BigDecimal price, int totalSeats) {
        if (totalSeats <= 0) {
            throw new IllegalArgumentException("totalSeats deve ser maior que zero");
        }
        this.id = UUID.randomUUID();
        this.origin = origin;
        this.destination = destination;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
        this.airline = airline;
        this.price = price;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
    }

    /**
     * Regra de negócio central do agregado: só permite reservar se houver
     * assentos disponíveis. Lança exceção de domínio em vez de retornar
     * boolean — força quem chama a tratar o caso de falta de disponibilidade.
     */
    public void reserveSeats(int passengers) {
        if (passengers <= 0) {
            throw new IllegalArgumentException("passengers deve ser maior que zero");
        }
        if (passengers > availableSeats) {
            throw new InsufficientAvailabilityException(this.id, passengers, availableSeats);
        }
        this.availableSeats -= passengers;
    }

    public void releaseSeats(int passengers) {
        this.availableSeats = Math.min(totalSeats, this.availableSeats + passengers);
    }

    public UUID getId() {
        return id;
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

    public String getAirline() {
        return airline;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public Long getVersion() {
        return version;
    }
}
