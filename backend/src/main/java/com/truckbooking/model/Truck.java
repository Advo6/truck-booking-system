package com.truckbooking.model;

import com.truckbooking.model.enums.TruckStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trucks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Truck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "truck_number", nullable = false)
    private String truckNumber;

    @Column(nullable = false, unique = true)
    private String registration;

    @Column(nullable = false)
    private String capacity;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TruckStatus status;

    @Column(length = 500)
    private String image;
}
