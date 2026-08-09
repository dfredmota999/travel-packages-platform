package com.travelplatform.carreservationservice.web;

import com.travelplatform.carreservationservice.domain.CarOffer;
import com.travelplatform.carreservationservice.domain.CarReservation;
import com.travelplatform.carreservationservice.service.CarOfferService;
import com.travelplatform.carreservationservice.web.dto.CarOfferResponse;
import com.travelplatform.carreservationservice.web.dto.CreateCarOfferRequest;
import com.travelplatform.carreservationservice.web.dto.CreateReservationRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cars")
public class CarOfferController {

    private final CarOfferService service;

    public CarOfferController(CarOfferService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarOfferResponse createOffer(@Valid @RequestBody CreateCarOfferRequest request) {
        CarOffer offer = new CarOffer(
                request.category(), request.model(), request.pickupDate(),
                request.returnDate(), request.dailyRate(), request.totalUnits());
        return CarOfferResponse.from(service.createOffer(offer));
    }

    @GetMapping("/{id}")
    public CarOfferResponse getOffer(@PathVariable UUID id) {
        return CarOfferResponse.from(service.findOfferById(id));
    }

    @PostMapping("/{offerId}/reservations")
    public ResponseEntity<CreateReservationRequest.Response> reserve(
            @PathVariable UUID offerId, @Valid @RequestBody CreateReservationRequest request) {
        CarReservation reservation = service.reserve(offerId, request.customerId());
        URI location = URI.create("/api/cars/reservations/" + reservation.getId());
        return ResponseEntity.created(location).body(CreateReservationRequest.Response.from(reservation));
    }

    @GetMapping("/reservations/{reservationId}")
    public CreateReservationRequest.Response getReservation(@PathVariable UUID reservationId) {
        return CreateReservationRequest.Response.from(service.findReservationById(reservationId));
    }
}
