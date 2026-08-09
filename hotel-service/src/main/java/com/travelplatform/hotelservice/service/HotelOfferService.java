package com.travelplatform.hotelservice.service;

import com.travelplatform.hotelservice.domain.HotelOffer;
import com.travelplatform.hotelservice.domain.HotelReservation;
import com.travelplatform.hotelservice.exception.HotelOfferNotFoundException;
import com.travelplatform.hotelservice.exception.HotelReservationNotFoundException;
import com.travelplatform.hotelservice.repository.HotelOfferRepository;
import com.travelplatform.hotelservice.repository.HotelReservationRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HotelOfferService {

    private static final int ROOMS_PER_RESERVATION = 1;

    private final HotelOfferRepository offerRepository;
    private final HotelReservationRepository reservationRepository;

    public HotelOfferService(HotelOfferRepository offerRepository,
                              HotelReservationRepository reservationRepository) {
        this.offerRepository = offerRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public HotelOffer createOffer(HotelOffer offer) {
        return offerRepository.save(offer);
    }

    @Transactional(readOnly = true)
    public HotelOffer findOfferById(UUID id) {
        return offerRepository.findById(id).orElseThrow(() -> new HotelOfferNotFoundException(id));
    }

    /** Cada reserva ocupa 1 quarto do tipo ofertado — os hóspedes (guests) são apenas informativos. */
    @Transactional
    public HotelReservation reserve(UUID offerId, String customerId, int guests) {
        HotelOffer offer = findOfferById(offerId);
        offer.reserveRooms(ROOMS_PER_RESERVATION);
        offerRepository.save(offer);

        HotelReservation reservation = new HotelReservation(offerId, customerId, guests);
        return reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public HotelReservation findReservationById(UUID id) {
        return reservationRepository.findById(id).orElseThrow(() -> new HotelReservationNotFoundException(id));
    }

    /** Compensação: usada pela saga quando outro item do pacote falha e esta reserva precisa ser desfeita. */
    @Transactional
    public void cancelReservation(UUID reservationId) {
        HotelReservation reservation = findReservationById(reservationId);
        reservation.cancel();

        HotelOffer offer = findOfferById(reservation.getOfferId());
        offer.releaseRooms(ROOMS_PER_RESERVATION);
        offerRepository.save(offer);
    }
}
