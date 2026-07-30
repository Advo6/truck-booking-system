package com.truckbooking.dto;

import com.truckbooking.model.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private String bookingReference;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private Long truckId;
    private String truckNumber;
    private String truckRegistration;
    private LocalDate pickupDate;
    private LocalDate returnDate;
    private String pickupAddress;
    private String deliveryAddress;
    private String cargoDescription;
    private BigDecimal weight;
    private String specialInstructions;
    private BookingStatus status;
    private LocalDateTime createdAt;
    private BigDecimal totalAmount;
}
