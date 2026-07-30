package com.truckbooking.config;

import com.truckbooking.model.Truck;
import com.truckbooking.model.User;
import com.truckbooking.model.enums.Role;
import com.truckbooking.model.enums.TruckStatus;
import com.truckbooking.repository.TruckRepository;
import com.truckbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TruckRepository truckRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            userRepository.save(User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build());
        }

        if (truckRepository.count() == 0) {
            truckRepository.save(Truck.builder()
                    .truckNumber("Truck 1")
                    .registration("CA 123-456 GP")
                    .capacity("34 Tonnes")
                    .description("Mercedes-Benz Actros Code 14 — ideal for long-haul freight and heavy cargo transport across South Africa.")
                    .status(TruckStatus.AVAILABLE)
                    .image("images/truck1.jpg")
                    .build());

            truckRepository.save(Truck.builder()
                    .truckNumber("Truck 2")
                    .registration("CA 789-012 GP")
                    .capacity("34 Tonnes")
                    .description("Volvo FH16 Code 14 — reliable heavy-duty truck for construction materials and bulk deliveries.")
                    .status(TruckStatus.AVAILABLE)
                    .image("images/truck2.jpg")
                    .build());
        }
    }
}
