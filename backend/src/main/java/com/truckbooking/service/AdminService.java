package com.truckbooking.service;

import com.truckbooking.dto.DashboardStats;
import com.truckbooking.model.enums.BookingStatus;
import com.truckbooking.model.enums.TruckStatus;
import com.truckbooking.repository.BookingRepository;
import com.truckbooking.repository.TruckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final java.util.List<BookingStatus> REVENUE_STATUSES = Arrays.asList(
            BookingStatus.APPROVED, BookingStatus.COMPLETED
    );

    private final BookingRepository bookingRepository;
    private final TruckRepository truckRepository;

    public DashboardStats getDashboardStats() {
        BigDecimal revenue = bookingRepository.sumRevenueByStatuses(REVENUE_STATUSES);

        return DashboardStats.builder()
                .totalBookings(bookingRepository.count())
                .todayBookings(bookingRepository.countTodayBookings(LocalDate.now()))
                .availableTrucks(truckRepository.countByStatus(TruckStatus.AVAILABLE))
                .bookedTrucks(truckRepository.countByStatus(TruckStatus.BOOKED))
                .pendingBookings(bookingRepository.findByStatus(BookingStatus.PENDING).size())
                .maintenanceTrucks(truckRepository.countByStatus(TruckStatus.MAINTENANCE))
                .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
                .build();
    }
}
