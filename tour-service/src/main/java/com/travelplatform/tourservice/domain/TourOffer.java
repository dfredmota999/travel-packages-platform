package com.travelplatform.tourservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Aggregate root do bounded context "Passeio". Representa uma oferta de
 * passeio/atividade numa data, com N vagas disponíveis.
 * @Version implementa lock otimista, mesma ideia dos outros serviços de produto.
 */
@Entity
@Table(name = "tour_offers")
public class TourOffer {

    @Id
    private UUID id;

    private String tourName;
    private String location;
    private LocalDate date;
    private BigDecimal pricePerPerson;
    private int totalSlots;
    private int availableSlots;

    @Version
    private Long version;

    protected TourOffer() {
        // exigido pelo JPA
    }

    public TourOffer(String tourName, String location, LocalDate date,
                      BigDecimal pricePerPerson, int totalSlots) {
        if (totalSlots <= 0) {
            throw new IllegalArgumentException("totalSlots deve ser maior que zero");
        }
        this.id = UUID.randomUUID();
        this.tourName = tourName;
        this.location = location;
        this.date = date;
        this.pricePerPerson = pricePerPerson;
        this.totalSlots = totalSlots;
        this.availableSlots = totalSlots;
    }

    public void reserveSlots(int participants) {
        if (participants <= 0) {
            throw new IllegalArgumentException("participants deve ser maior que zero");
        }
        if (participants > availableSlots) {
            throw new InsufficientAvailabilityException(this.id, participants, availableSlots);
        }
        this.availableSlots -= participants;
    }

    public void releaseSlots(int participants) {
        this.availableSlots = Math.min(totalSlots, this.availableSlots + participants);
    }

    public UUID getId() {
        return id;
    }

    public String getTourName() {
        return tourName;
    }

    public String getLocation() {
        return location;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getPricePerPerson() {
        return pricePerPerson;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public int getAvailableSlots() {
        return availableSlots;
    }

    public Long getVersion() {
        return version;
    }
}
