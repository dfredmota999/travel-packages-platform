package com.travelplatform.tourservice.web;

import com.travelplatform.tourservice.domain.TourOffer;
import com.travelplatform.tourservice.domain.TourReservation;
import com.travelplatform.tourservice.service.TourOfferService;
import com.travelplatform.tourservice.web.dto.CreateReservationRequest;
import com.travelplatform.tourservice.web.dto.CreateTourOfferRequest;
import com.travelplatform.tourservice.web.dto.TourOfferResponse;
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
@RequestMapping("/api/tours")
public class TourOfferController {

    private final TourOfferService service;

    public TourOfferController(TourOfferService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TourOfferResponse createOffer(@Valid @RequestBody CreateTourOfferRequest request) {
        TourOffer offer = new TourOffer(
                request.tourName(), request.location(), request.date(),
                request.pricePerPerson(), request.totalSlots());
        return TourOfferResponse.from(service.createOffer(offer));
    }

    @GetMapping("/{id}")
    public TourOfferResponse getOffer(@PathVariable UUID id) {
        return TourOfferResponse.from(service.findOfferById(id));
    }

    @PostMapping("/{offerId}/reservations")
    public ResponseEntity<CreateReservationRequest.Response> reserve(
            @PathVariable UUID offerId, @Valid @RequestBody CreateReservationRequest request) {
        TourReservation reservation = service.reserve(offerId, request.customerId(), request.participants());
        URI location = URI.create("/api/tours/reservations/" + reservation.getId());
        return ResponseEntity.created(location).body(CreateReservationRequest.Response.from(reservation));
    }

    @GetMapping("/reservations/{reservationId}")
    public CreateReservationRequest.Response getReservation(@PathVariable UUID reservationId) {
        return CreateReservationRequest.Response.from(service.findReservationById(reservationId));
    }
}
