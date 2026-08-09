package com.travelplatform.packageservice.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class PaymentInfo {

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
