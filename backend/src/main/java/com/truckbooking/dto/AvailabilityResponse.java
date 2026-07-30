package com.truckbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponse {
    private Long truckId;
    private String truckNumber;
    private boolean available;
    private List<String> bookedDates;
    private Map<String, String> dateStatus;
}
