package com.truckbooking.service;

import com.truckbooking.dto.AuthResponse;
import com.truckbooking.dto.LoginRequest;
import com.truckbooking.dto.RegisterRequest;
import com.truckbooking.model.Customer;
import com.truckbooking.model.User;
import com.truckbooking.model.enums.Role;
import com.truckbooking.repository.CustomerRepository;
import com.truckbooking.repository.UserRepository;
import com.truckbooking.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwtUtils.generateToken(user.getUsername());
        Long customerId = user.getCustomer() != null ? user.getCustomer().getId() : null;

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .userId(user.getId())
                .customerId(customerId)
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .build();

        Customer customer = Customer.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .user(user)
                .build();

        user.setCustomer(customer);
        userRepository.save(user);

        String token = jwtUtils.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .userId(user.getId())
                .customerId(customer.getId())
                .build();
    }
}
