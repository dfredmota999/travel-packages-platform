package com.travelplatform.flightservice.web;

import com.travelplatform.flightservice.domain.FlightOffer;
import com.travelplatform.flightservice.domain.FlightReservation;
import com.travelplatform.flightservice.service.FlightOfferService;
import com.travelplatform.flightservice.web.dto.CreateFlightOfferRequest;
import com.travelplatform.flightservice.web.dto.CreateReservationRequest;
import com.travelplatform.flightservice.web.dto.FlightOfferResponse;
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
@RequestMapping("/api/flights")
public class FlightOfferController {

    private final FlightOfferService service;

    public FlightOfferController(FlightOfferService service) {
        this.service = service;
    }

    /** Criação de oferta = ação de "catálogo" (num cenário real viria de um back-office/admin). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FlightOfferResponse createOffer(@Valid @RequestBody CreateFlightOfferRequest request) {
        FlightOffer offer = new FlightOffer(
                request.origin(), request.destination(), request.departureDate(),
                request.returnDate(), request.airline(), request.price(), request.totalSeats());
        return FlightOfferResponse.from(service.createOffer(offer));
    }

    @GetMapping("/{id}")
    public FlightOfferResponse getOffer(@PathVariable UUID id) {
        return FlightOfferResponse.from(service.findOfferById(id));
    }

    /**
     * Reserva síncrona via REST por enquanto. Quando ligarmos a saga do
     * package-service, isso vira um consumer de RabbitMQ escutando um comando
     * "cmd.reserve-flight" — a lógica de negócio (FlightOfferService) não muda,
     * só a forma como ela é acionada.
     */
    @PostMapping("/{offerId}/reservations")
    public ResponseEntity<CreateReservationRequest.Response> reserve(
            @PathVariable UUID offerId, @Valid @RequestBody CreateReservationRequest request) {
        FlightReservation reservation = service.reserveSeats(offerId, request.customerId(), request.passengers());
        URI location = URI.create("/api/flights/reservations/" + reservation.getId());
        return ResponseEntity.created(location).body(CreateReservationRequest.Response.from(reservation));
    }

    @GetMapping("/reservations/{reservationId}")
    public CreateReservationRequest.Response getReservation(@PathVariable UUID reservationId) {
        return CreateReservationRequest.Response.from(service.findReservationById(reservationId));
    }
}
