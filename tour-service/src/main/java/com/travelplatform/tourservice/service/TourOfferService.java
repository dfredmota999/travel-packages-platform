package com.travelplatform.tourservice.service;

import com.travelplatform.tourservice.domain.TourOffer;
import com.travelplatform.tourservice.domain.TourReservation;
import com.travelplatform.tourservice.exception.TourOfferNotFoundException;
import com.travelplatform.tourservice.exception.TourReservationNotFoundException;
import com.travelplatform.tourservice.repository.TourOfferRepository;
import com.travelplatform.tourservice.repository.TourReservationRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TourOfferService {

    private final TourOfferRepository offerRepository;
    private final TourReservationRepository reservationRepository;

    public TourOfferService(TourOfferRepository offerRepository, TourReservationRepository reservationRepository) {
        this.offerRepository = offerRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public TourOffer createOffer(TourOffer offer) {
        return offerRepository.save(offer);
    }

    @Transactional(readOnly = true)
    public TourOffer findOfferById(UUID id) {
        return offerRepository.findById(id).orElseThrow(() -> new TourOfferNotFoundException(id));
    }

    @Transactional
    public TourReservation reserve(UUID offerId, String customerId, int participants) {
        TourOffer offer = findOfferById(offerId);
        offer.reserveSlots(participants);
        offerRepository.save(offer);

        TourReservation reservation = new TourReservation(offerId, customerId, participants);
        return reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public TourReservation findReservationById(UUID id) {
        return reservationRepository.findById(id).orElseThrow(() -> new TourReservationNotFoundException(id));
    }

    /** Compensação: usada pela saga quando outro item do pacote falha e esta reserva precisa ser desfeita. */
    @Transactional
    public void cancelReservation(UUID reservationId) {
        TourReservation reservation = findReservationById(reservationId);
        reservation.cancel();

        TourOffer offer = findOfferById(reservation.getOfferId());
        offer.releaseSlots(reservation.getParticipants());
        offerRepository.save(offer);
    }
}
