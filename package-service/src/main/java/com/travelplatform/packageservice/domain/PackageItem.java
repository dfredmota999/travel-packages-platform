package com.travelplatform.packageservice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Item do pacote — voo, hotel, carro ou passeio. Propositalmente enxuto: o
 * package-service NÃO duplica o catálogo (datas, preço, hóspedes...) de cada
 * serviço de produto, só guarda o suficiente pra orquestrar a saga:
 *
 * - offerId: a oferta que o cliente escolheu (já existe no flight-service/
 *   hotel-service/etc, criada por lá antes do pacote existir)
 * - quantity: passageiros/hóspedes/participantes — o único dado que o
 *   serviço de produto realmente precisa pra processar a reserva
 * - reservationId: devolvido pelo serviço de produto quando confirma
 * - status: PENDING → CONFIRMED/REJECTED, ou CANCELLED se compensado
 *
 * Isso é uma tabela só (package_items) com um discriminador de tipo, em vez
 * de 4 tabelas quase idênticas — menos repetição, mais fácil de estender
 * (novo tipo de item = um valor novo no enum, não uma classe nova).
 */
@Entity
@Table(name = "package_items")
public class PackageItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private TravelPackage travelPackage;

    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    private String offerId;
    private int quantity;
    private String reservationId;

    @Enumerated(EnumType.STRING)
    private BookingItemStatus status;

    protected PackageItem() {
        // exigido pelo JPA
    }

    public PackageItem(ItemType itemType, String offerId, int quantity) {
        this.itemType = itemType;
        this.offerId = offerId;
        this.quantity = quantity;
        this.status = BookingItemStatus.PENDING;
    }

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

    public UUID getId() {
        return id;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public String getOfferId() {
        return offerId;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getReservationId() {
        return reservationId;
    }

    public BookingItemStatus getStatus() {
        return status;
    }
}
