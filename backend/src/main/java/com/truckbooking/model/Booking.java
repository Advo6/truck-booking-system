package com.truckbooking.model;

import com.truckbooking.model.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "truck_id", nullable = false)
    private Truck truck;

    @Column(name = "pickup_date", nullable = false)
    private LocalDate pickupDate;

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Column(name = "pickup_address", nullable = false, length = 500)
    private String pickupAddress;

    @Column(name = "delivery_address", nullable = false, length = 500)
    private String deliveryAddress;

    @Column(name = "cargo_description", length = 1000)
    private String cargoDescription;

    private BigDecimal weight;

    @Column(name = "special_instructions", length = 1000)
    private String specialInstructions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "booking_reference", nullable = false, unique = true)
    private String bookingReference;

    /** Estimated total based on rental days × daily rate. */
    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = BookingStatus.PENDING;
        }
    }
}
