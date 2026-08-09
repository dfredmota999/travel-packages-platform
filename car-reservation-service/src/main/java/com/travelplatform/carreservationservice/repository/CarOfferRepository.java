package com.travelplatform.carreservationservice.repository;

import com.travelplatform.carreservationservice.domain.CarOffer;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarOfferRepository extends JpaRepository<CarOffer, UUID> {
}
