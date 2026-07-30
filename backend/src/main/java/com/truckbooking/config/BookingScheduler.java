package com.truckbooking.config;

import com.truckbooking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingScheduler {

    private final BookingService bookingService;

    /** Release trucks daily at midnight when bookings expire. */
    @Scheduled(cron = "0 0 0 * * *")
    public void releaseExpiredBookings() {
        bookingService.releaseExpiredBookings();
    }
}
