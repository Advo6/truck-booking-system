package com.truckbooking.dto;

import com.truckbooking.model.enums.BookingStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookingRequest {
    @NotBlank
    private String fullName;

    @NotBlank
    private String phone;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String pickupAddress;

    @NotBlank
    private String deliveryAddress;

    @NotNull
    private LocalDate pickupDate;

    @NotNull
    private LocalDate returnDate;

    private String cargoDescription;

    @DecimalMin("0.0")
    private BigDecimal weight;

    private String specialInstructions;

    private Long truckId;
}
