package com.truckbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private long totalBookings;
    private long todayBookings;
    private long availableTrucks;
    private long bookedTrucks;
    private long pendingBookings;
    private long maintenanceTrucks;
    private BigDecimal totalRevenue;
}
