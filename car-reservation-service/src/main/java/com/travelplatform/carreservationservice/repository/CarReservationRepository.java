package com.travelplatform.carreservationservice.repository;

import com.travelplatform.carreservationservice.domain.CarReservation;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarReservationRepository extends JpaRepository<CarReservation, UUID> {
}
