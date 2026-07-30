package com.truckbooking.repository;

import com.truckbooking.model.Truck;
import com.truckbooking.model.enums.TruckStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TruckRepository extends JpaRepository<Truck, Long> {
    Optional<Truck> findByRegistration(String registration);
    List<Truck> findByStatus(TruckStatus status);
    long countByStatus(TruckStatus status);
}
