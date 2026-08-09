package com.travelplatform.tourservice.repository;

import com.travelplatform.tourservice.domain.TourOffer;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourOfferRepository extends JpaRepository<TourOffer, UUID> {
}
