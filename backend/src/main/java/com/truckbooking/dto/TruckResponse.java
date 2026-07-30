package com.truckbooking.dto;

import com.truckbooking.model.enums.TruckStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruckResponse {
    private Long id;
    private String truckNumber;
    private String registration;
    private String capacity;
    private String description;
    private TruckStatus status;
    private String image;
}
