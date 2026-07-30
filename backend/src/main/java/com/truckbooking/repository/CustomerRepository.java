package com.truckbooking.repository;

import com.truckbooking.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByUserId(Long userId);

    @Query("SELECT c FROM Customer c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR c.email LIKE CONCAT('%', :query, '%') OR c.phone LIKE CONCAT('%', :query, '%')")
    List<Customer> search(@Param("query") String query);
}
