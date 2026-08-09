package com.travelplatform.packageservice.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate root do domínio de compra de pacotes.
 * Toda alteração de estado dos itens (voo/hotel/carro/passeio/pagamento) deve
 * passar por aqui — os value objects não são acessados/persistidos fora do agregado.
 *
 * A saga (próxima etapa) vai orquestrar comandos assíncronos via RabbitMQ e,
 * a cada resposta recebida, chamar um dos métodos de domínio abaixo para
 * avançar o estado do pacote.
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

    // Cada @Embeddable de item (Flight/Hotel/CarRental/Tour) tem campos com o
    // mesmo nome (offerId, status, reservationId...). Sem @AttributeOverrides,
    // o Hibernate geraria colunas duplicadas na tabela travel_packages.
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "offerId", column = @Column(name = "flight_offer_id")),
            @AttributeOverride(name = "origin", column = @Column(name = "flight_origin")),
            @AttributeOverride(name = "destination", column = @Column(name = "flight_destination")),
            @AttributeOverride(name = "departureDate", column = @Column(name = "flight_departure_date")),
            @AttributeOverride(name = "returnDate", column = @Column(name = "flight_return_date")),
            @AttributeOverride(name = "passengers", column = @Column(name = "flight_passengers")),
            @AttributeOverride(name = "reservationId", column = @Column(name = "flight_reservation_id")),
            @AttributeOverride(name = "status", column = @Column(name = "flight_status"))
    })
    private FlightBooking flightBooking;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "offerId", column = @Column(name = "hotel_offer_id")),
            @AttributeOverride(name = "checkIn", column = @Column(name = "hotel_check_in")),
            @AttributeOverride(name = "checkOut", column = @Column(name = "hotel_check_out")),
            @AttributeOverride(name = "roomType", column = @Column(name = "hotel_room_type")),
            @AttributeOverride(name = "guests", column = @Column(name = "hotel_guests")),
            @AttributeOverride(name = "reservationId", column = @Column(name = "hotel_reservation_id")),
            @AttributeOverride(name = "status", column = @Column(name = "hotel_status"))
    })
    private HotelBooking hotelBooking;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "offerId", column = @Column(name = "car_offer_id")),
            @AttributeOverride(name = "pickupDate", column = @Column(name = "car_pickup_date")),
            @AttributeOverride(name = "returnDate", column = @Column(name = "car_return_date")),
            @AttributeOverride(name = "category", column = @Column(name = "car_category")),
            @AttributeOverride(name = "reservationId", column = @Column(name = "car_reservation_id")),
            @AttributeOverride(name = "status", column = @Column(name = "car_status"))
    })
    private CarRentalBooking carRentalBooking;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "offerId", column = @Column(name = "tour_offer_id")),
            @AttributeOverride(name = "date", column = @Column(name = "tour_date")),
            @AttributeOverride(name = "participants", column = @Column(name = "tour_participants")),
            @AttributeOverride(name = "reservationId", column = @Column(name = "tour_reservation_id")),
            @AttributeOverride(name = "status", column = @Column(name = "tour_status"))
    })
    private TourBooking tourBooking;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "type", column = @Column(name = "payment_type")),
            @AttributeOverride(name = "installments", column = @Column(name = "payment_installments")),
            @AttributeOverride(name = "transactionId", column = @Column(name = "payment_transaction_id")),
            @AttributeOverride(name = "status", column = @Column(name = "payment_status"))
    })
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
        this.flightBooking = new FlightBooking(null, null, null, null, null, 0);
        this.hotelBooking = new HotelBooking(null, null, null, null, 0);
        this.carRentalBooking = new CarRentalBooking(null, null, null, null);
        this.tourBooking = new TourBooking(null, null, 0);
    }

    /**
     * Factory method — força a criação do pacote a passar por aqui em vez de
     * expor um construtor público, garantindo que o agregado nasça em estado válido.
     */
    public static TravelPackage create(String customerId,
                                        FlightBooking flightBooking,
                                        HotelBooking hotelBooking,
                                        CarRentalBooking carRentalBooking,
                                        TourBooking tourBooking,
                                        PaymentInfo paymentInfo) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId é obrigatório");
        }
        if (flightBooking == null && hotelBooking == null
                && carRentalBooking == null && tourBooking == null) {
            throw new IllegalArgumentException("o pacote precisa ter ao menos um item (voo, hotel, carro ou passeio)");
        }

        TravelPackage travelPackage = new TravelPackage(customerId);
        if (flightBooking != null) {
            travelPackage.flightBooking = flightBooking;
        }
        if (hotelBooking != null) {
            travelPackage.hotelBooking = hotelBooking;
        }
        if (carRentalBooking != null) {
            travelPackage.carRentalBooking = carRentalBooking;
        }
        if (tourBooking != null) {
            travelPackage.tourBooking = tourBooking;
        }
        travelPackage.paymentInfo = paymentInfo;
        return travelPackage;
    }

    public void startProcessing() {
        requireStatus(PackageStatus.CREATED);
        this.status = PackageStatus.PROCESSING;
        touch();
    }

    /** Chamado pela saga quando todos os itens solicitados foram confirmados. */
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

    /** Chamado quando algum item é rejeitado — a saga inicia compensação dos demais. */
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

    /** Verifica se todos os itens solicitados (isRequested) já estão CONFIRMED. */
    public boolean allRequestedItemsConfirmed() {
        return isConfirmedOrNotRequested(flightBooking.isRequested(), flightBooking.getStatus())
                && isConfirmedOrNotRequested(hotelBooking.isRequested(), hotelBooking.getStatus())
                && isConfirmedOrNotRequested(carRentalBooking.isRequested(), carRentalBooking.getStatus())
                && isConfirmedOrNotRequested(tourBooking.isRequested(), tourBooking.getStatus());
    }

    private boolean isConfirmedOrNotRequested(boolean requested, BookingItemStatus status) {
        return !requested || status == BookingItemStatus.CONFIRMED;
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

    public FlightBooking getFlightBooking() {
        return flightBooking;
    }

    public HotelBooking getHotelBooking() {
        return hotelBooking;
    }

    public CarRentalBooking getCarRentalBooking() {
        return carRentalBooking;
    }

    public TourBooking getTourBooking() {
        return tourBooking;
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }
}
