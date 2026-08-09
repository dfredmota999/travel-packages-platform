package com.travelplatform.hotelservice.repository;

import com.travelplatform.hotelservice.domain.HotelReservation;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelReservationRepository extends JpaRepository<HotelReservation, UUID> {
}
