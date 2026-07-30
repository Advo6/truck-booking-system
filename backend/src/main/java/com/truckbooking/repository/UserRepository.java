package com.truckbooking.repository;

import com.truckbooking.model.User;
import com.truckbooking.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    long countByRole(Role role);
}
