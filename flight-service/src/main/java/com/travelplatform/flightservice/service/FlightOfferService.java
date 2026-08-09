package com.travelplatform.flightservice.service;

import com.travelplatform.flightservice.domain.FlightOffer;
import com.travelplatform.flightservice.domain.FlightReservation;
import com.travelplatform.flightservice.exception.FlightOfferNotFoundException;
import com.travelplatform.flightservice.exception.FlightReservationNotFoundException;
import com.travelplatform.flightservice.repository.FlightOfferRepository;
import com.travelplatform.flightservice.repository.FlightReservationRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FlightOfferService {

    private final FlightOfferRepository offerRepository;
    private final FlightReservationRepository reservationRepository;

    public FlightOfferService(FlightOfferRepository offerRepository,
                               FlightReservationRepository reservationRepository) {
        this.offerRepository = offerRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public FlightOffer createOffer(FlightOffer offer) {
        return offerRepository.save(offer);
    }

    @Transactional(readOnly = true)
    public FlightOffer findOfferById(UUID id) {
        return offerRepository.findById(id).orElseThrow(() -> new FlightOfferNotFoundException(id));
    }

    /**
     * Debita os assentos da oferta e cria o registro de reserva na mesma
     * transação. Se outra requisição concorrente já tiver alterado a oferta
     * (lock otimista via @Version), o Spring lança
     * ObjectOptimisticLockingFailureException e nada é persistido — o
     * GlobalExceptionHandler traduz isso pra um 409 Conflict.
     */
    @Transactional
    public FlightReservation reserveSeats(UUID offerId, String customerId, int passengers) {
        FlightOffer offer = findOfferById(offerId);
        offer.reserveSeats(passengers); // lança InsufficientAvailabilityException se não houver assentos
        offerRepository.save(offer);

        FlightReservation reservation = new FlightReservation(offerId, customerId, passengers);
        return reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public FlightReservation findReservationById(UUID id) {
        return reservationRepository.findById(id).orElseThrow(() -> new FlightReservationNotFoundException(id));
    }

    /** Compensação: usada pela saga quando outro item do pacote falha e este voo precisa ser desfeito. */
    @Transactional
    public void cancelReservation(UUID reservationId) {
        FlightReservation reservation = findReservationById(reservationId);
        reservation.cancel();

        FlightOffer offer = findOfferById(reservation.getOfferId());
        offer.releaseSeats(reservation.getPassengers());
        offerRepository.save(offer);
    }
}
