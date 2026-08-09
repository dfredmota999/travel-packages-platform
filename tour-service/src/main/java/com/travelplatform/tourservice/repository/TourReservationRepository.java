package com.travelplatform.tourservice.repository;

import com.travelplatform.tourservice.domain.TourReservation;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourReservationRepository extends JpaRepository<TourReservation, UUID> {
}
