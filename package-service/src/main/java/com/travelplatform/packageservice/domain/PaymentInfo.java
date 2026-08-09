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
import java.util.UUID;

@Entity
@Table(name = "payment_info")
public class PaymentInfo {

    @Id
    private UUID packageId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "package_id")
    private TravelPackage travelPackage;

    @Enumerated(EnumType.STRING)
    private PaymentMethodType type;

    private Integer installments;
    private String transactionId;

    @Enumerated(EnumType.STRING)
    private BookingItemStatus status = BookingItemStatus.NOT_REQUESTED;

    protected PaymentInfo() {
    }

    public PaymentInfo(PaymentMethodType type, Integer installments) {
        this.type = type;
        this.installments = installments;
        this.status = BookingItemStatus.PENDING;
    }

    void attachTo(TravelPackage travelPackage) {
        this.travelPackage = travelPackage;
    }

    public void confirm(String transactionId) {
        this.transactionId = transactionId;
        this.status = BookingItemStatus.CONFIRMED;
    }

    public void reject() {
        this.status = BookingItemStatus.REJECTED;
    }

    public PaymentMethodType getType() {
        return type;
    }

    public Integer getInstallments() {
        return installments;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public BookingItemStatus getStatus() {
        return status;
    }
}
