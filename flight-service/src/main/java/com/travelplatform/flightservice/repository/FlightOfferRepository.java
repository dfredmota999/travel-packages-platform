package com.travelplatform.flightservice.repository;

import com.travelplatform.flightservice.domain.FlightOffer;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightOfferRepository extends JpaRepository<FlightOffer, UUID> {
}
