package com.travelplatform.flightservice.repository;

import com.travelplatform.flightservice.domain.FlightReservation;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightReservationRepository extends JpaRepository<FlightReservation, UUID> {
}
