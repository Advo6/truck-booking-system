package com.truckbooking.dto;

import com.truckbooking.model.enums.TruckStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TruckRequest {
    @NotBlank
    private String truckNumber;

    @NotBlank
    private String registration;

    @NotBlank
    private String capacity;

    private String description;
    private TruckStatus status;
    private String image;
}
