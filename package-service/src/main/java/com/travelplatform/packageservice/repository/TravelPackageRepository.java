package com.travelplatform.packageservice.repository;

import com.travelplatform.packageservice.domain.TravelPackage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelPackageRepository extends JpaRepository<TravelPackage, UUID> {
}
