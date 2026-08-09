package com.travelplatform.carreservationservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Aggregate root do bounded context "Carro". Representa uma oferta de
 * categoria de veículo, num período, com N unidades disponíveis na frota.
 * @Version implementa lock otimista, mesma ideia dos outros serviços de produto.
 */
@Entity
@Table(name = "car_offers")
public class CarOffer {

    @Id
    private UUID id;

    private String category;
    private String model;
    private LocalDate pickupDate;
    private LocalDate returnDate;
    private BigDecimal dailyRate;
    private int totalUnits;
    private int availableUnits;

    @Version
    private Long version;

    protected CarOffer() {
        // exigido pelo JPA
    }

    public CarOffer(String category, String model, LocalDate pickupDate, LocalDate returnDate,
                     BigDecimal dailyRate, int totalUnits) {
        if (totalUnits <= 0) {
            throw new IllegalArgumentException("totalUnits deve ser maior que zero");
        }
        this.id = UUID.randomUUID();
        this.category = category;
        this.model = model;
        this.pickupDate = pickupDate;
        this.returnDate = returnDate;
        this.dailyRate = dailyRate;
        this.totalUnits = totalUnits;
        this.availableUnits = totalUnits;
    }

    public void reserveUnit() {
        if (availableUnits <= 0) {
            throw new InsufficientAvailabilityException(this.id, 1, availableUnits);
        }
        this.availableUnits -= 1;
    }

    public void releaseUnit() {
        this.availableUnits = Math.min(totalUnits, this.availableUnits + 1);
    }

    public UUID getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getModel() {
        return model;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public int getTotalUnits() {
        return totalUnits;
    }

    public int getAvailableUnits() {
        return availableUnits;
    }

    public Long getVersion() {
        return version;
    }
}
