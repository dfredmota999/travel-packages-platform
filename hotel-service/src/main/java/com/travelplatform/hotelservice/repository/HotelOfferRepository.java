package com.travelplatform.hotelservice.repository;

import com.travelplatform.hotelservice.domain.HotelOffer;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelOfferRepository extends JpaRepository<HotelOffer, UUID> {
}
