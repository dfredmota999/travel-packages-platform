package com.travelplatform.hotelservice.web;

import com.travelplatform.hotelservice.domain.HotelOffer;
import com.travelplatform.hotelservice.domain.HotelReservation;
import com.travelplatform.hotelservice.service.HotelOfferService;
import com.travelplatform.hotelservice.web.dto.CreateHotelOfferRequest;
import com.travelplatform.hotelservice.web.dto.CreateReservationRequest;
import com.travelplatform.hotelservice.web.dto.HotelOfferResponse;
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
@RequestMapping("/api/hotels")
public class HotelOfferController {

    private final HotelOfferService service;

    public HotelOfferController(HotelOfferService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HotelOfferResponse createOffer(@Valid @RequestBody CreateHotelOfferRequest request) {
        HotelOffer offer = new HotelOffer(
                request.hotelName(), request.city(), request.roomType(),
                request.checkIn(), request.checkOut(), request.pricePerNight(), request.totalRooms());
        return HotelOfferResponse.from(service.createOffer(offer));
    }

    @GetMapping("/{id}")
    public HotelOfferResponse getOffer(@PathVariable UUID id) {
        return HotelOfferResponse.from(service.findOfferById(id));
    }

    @PostMapping("/{offerId}/reservations")
    public ResponseEntity<CreateReservationRequest.Response> reserve(
            @PathVariable UUID offerId, @Valid @RequestBody CreateReservationRequest request) {
        HotelReservation reservation = service.reserve(offerId, request.customerId(), request.guests());
        URI location = URI.create("/api/hotels/reservations/" + reservation.getId());
        return ResponseEntity.created(location).body(CreateReservationRequest.Response.from(reservation));
    }

    @GetMapping("/reservations/{reservationId}")
    public CreateReservationRequest.Response getReservation(@PathVariable UUID reservationId) {
        return CreateReservationRequest.Response.from(service.findReservationById(reservationId));
    }
}
