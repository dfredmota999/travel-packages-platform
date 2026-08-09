package com.travelplatform.carreservationservice.service;

import com.travelplatform.carreservationservice.domain.CarOffer;
import com.travelplatform.carreservationservice.domain.CarReservation;
import com.travelplatform.carreservationservice.exception.CarOfferNotFoundException;
import com.travelplatform.carreservationservice.exception.CarReservationNotFoundException;
import com.travelplatform.carreservationservice.repository.CarOfferRepository;
import com.travelplatform.carreservationservice.repository.CarReservationRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarOfferService {

    private final CarOfferRepository offerRepository;
    private final CarReservationRepository reservationRepository;

    public CarOfferService(CarOfferRepository offerRepository, CarReservationRepository reservationRepository) {
        this.offerRepository = offerRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public CarOffer createOffer(CarOffer offer) {
        return offerRepository.save(offer);
    }

    @Transactional(readOnly = true)
    public CarOffer findOfferById(UUID id) {
        return offerRepository.findById(id).orElseThrow(() -> new CarOfferNotFoundException(id));
    }

    @Transactional
    public CarReservation reserve(UUID offerId, String customerId) {
        CarOffer offer = findOfferById(offerId);
        offer.reserveUnit();
        offerRepository.save(offer);

        CarReservation reservation = new CarReservation(offerId, customerId);
        return reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public CarReservation findReservationById(UUID id) {
        return reservationRepository.findById(id).orElseThrow(() -> new CarReservationNotFoundException(id));
    }

    /** Compensação: usada pela saga quando outro item do pacote falha e esta reserva precisa ser desfeita. */
    @Transactional
    public void cancelReservation(UUID reservationId) {
        CarReservation reservation = findReservationById(reservationId);
        reservation.cancel();

        CarOffer offer = findOfferById(reservation.getOfferId());
        offer.releaseUnit();
        offerRepository.save(offer);
    }
}
