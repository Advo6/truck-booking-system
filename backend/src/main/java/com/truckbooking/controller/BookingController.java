package com.truckbooking.controller;

import com.truckbooking.dto.*;
import com.truckbooking.service.BookingService;
import com.truckbooking.service.TruckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final TruckService truckService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@Valid @RequestBody BookingRequest request) {
        BookingResponse booking = bookingService.createBooking(request);
        return ResponseEntity.ok(ApiResponse.ok("Booking created successfully", booking));
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<ApiResponse<BookingResponse>> getByReference(@PathVariable String reference) {
        return ResponseEntity.ok(ApiResponse.ok(bookingService.getByReference(reference)));
    }

    @GetMapping("/availability")
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>> getAvailability(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.ok(truckService.getAvailability(year, month)));
    }

    /** Logged-in customer: view their booking history. */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponse.ok(bookingService.getMyBookings(user.getUsername())));
    }

    /** Guest or customer: look up bookings by email address. */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getHistory(@RequestParam String email) {
        return ResponseEntity.ok(ApiResponse.ok(bookingService.getBookingsByEmail(email)));
    }
}
