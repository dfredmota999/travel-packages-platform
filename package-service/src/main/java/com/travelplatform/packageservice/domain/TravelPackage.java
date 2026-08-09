package com.travelplatform.packageservice.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate root. `items` é uma lista enxuta (ver PackageItem) — só entram
 * na lista os itens que o cliente realmente pediu, então não existe mais o
 * conceito de "item NOT_REQUESTED" que tínhamos antes com os 4 campos fixos.
 *
 * A saga (SagaOrchestrator) chama confirmItem/rejectItem/cancelItem conforme
 * os eventos chegam do RabbitMQ, e este agregado decide as transições de
 * status do pacote como um todo.
 */
@Entity
@Table(name = "travel_packages")
public class TravelPackage {

    @Id
    private UUID id;

    private String customerId;

    @Enumerated(EnumType.STRING)
    private PackageStatus status;

    private Instant createdAt;
    private Instant updatedAt;

    @OneToMany(mappedBy = "travelPackage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PackageItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "travelPackage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PaymentInfo paymentInfo;

    protected TravelPackage() {
        // exigido pelo JPA
    }

    private TravelPackage(String customerId) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.status = PackageStatus.CREATED;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static TravelPackage create(String customerId, List<PackageItem> requestedItems, PaymentInfo paymentInfo) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId é obrigatório");
        }
        if (requestedItems == null || requestedItems.isEmpty()) {
            throw new IllegalArgumentException("o pacote precisa ter ao menos um item (voo, hotel, carro ou passeio)");
        }

        TravelPackage travelPackage = new TravelPackage(customerId);
        for (PackageItem item : requestedItems) {
            item.attachTo(travelPackage);
            travelPackage.items.add(item);
        }
        paymentInfo.attachTo(travelPackage);
        travelPackage.paymentInfo = paymentInfo;
        return travelPackage;
    }

    public void startProcessing() {
        requireStatus(PackageStatus.CREATED);
        this.status = PackageStatus.PROCESSING;
        touch();
    }

    public void confirmItem(ItemType type, String reservationId) {
        findItem(type).confirm(reservationId);
        touch();
    }

    public void rejectItem(ItemType type) {
        findItem(type).reject();
        touch();
    }

    public void cancelItem(ItemType type) {
        findItem(type).cancel();
        touch();
    }

    public boolean hasItem(ItemType type) {
        return items.stream().anyMatch(i -> i.getItemType() == type);
    }

    /** Itens já CONFIRMED, exceto o tipo informado — é pra esses que a saga manda cmd.cancel na compensação. */
    public List<PackageItem> confirmedItemsExcept(ItemType exceptType) {
        return items.stream()
                .filter(i -> i.getItemType() != exceptType && i.getStatus() == BookingItemStatus.CONFIRMED)
                .toList();
    }

    public boolean allItemsConfirmed() {
        return items.stream().allMatch(i -> i.getStatus() == BookingItemStatus.CONFIRMED);
    }

    public void markAwaitingPayment() {
        requireStatus(PackageStatus.PROCESSING);
        this.status = PackageStatus.AWAITING_PAYMENT;
        touch();
    }

    public void confirm() {
        requireStatus(PackageStatus.AWAITING_PAYMENT);
        this.status = PackageStatus.CONFIRMED;
        touch();
    }

    public void startCompensating() {
        this.status = PackageStatus.COMPENSATING;
        touch();
    }

    public void cancel() {
        this.status = PackageStatus.CANCELLED;
        touch();
    }

    public void fail() {
        this.status = PackageStatus.FAILED;
        touch();
    }

    private PackageItem findItem(ItemType type) {
        return items.stream()
                .filter(i -> i.getItemType() == type)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Pacote não tem item do tipo " + type));
    }

    private void requireStatus(PackageStatus expected) {
        if (this.status != expected) {
            throw new IllegalStateException(
                    "Transição inválida: esperado status " + expected + " mas pacote está em " + this.status);
        }
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public PackageStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<PackageItem> getItems() {
        return items;
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }
}
