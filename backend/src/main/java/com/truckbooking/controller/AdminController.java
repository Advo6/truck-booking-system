package com.truckbooking.controller;

import com.truckbooking.dto.*;
import com.truckbooking.model.enums.BookingStatus;
import com.truckbooking.service.AdminService;
import com.truckbooking.service.BookingService;
import com.truckbooking.service.CustomerService;
import com.truckbooking.service.TruckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final BookingService bookingService;
    private final TruckService truckService;
    private final CustomerService customerService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getDashboardStats()));
    }

    // Bookings
    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAllBookings(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo) {
        return ResponseEntity.ok(ApiResponse.ok(
                bookingService.filterBookings(search, status, dateFrom, dateTo)));
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable Long id) {
        return bookingService.getAllBookings().stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .map(b -> ResponseEntity.ok(ApiResponse.ok(b)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/bookings/{id}/status")
    public ResponseEntity<ApiResponse<BookingResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam BookingStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(bookingService.updateStatus(id, status)));
    }

    @PutMapping("/bookings/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(bookingService.updateBooking(id, request)));
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok(ApiResponse.ok("Booking deleted", null));
    }

    // Trucks
    @PostMapping("/trucks")
    public ResponseEntity<ApiResponse<TruckResponse>> createTruck(@Valid @RequestBody TruckRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(truckService.createTruck(request)));
    }

    @PutMapping("/trucks/{id}")
    public ResponseEntity<ApiResponse<TruckResponse>> updateTruck(
            @PathVariable Long id,
            @Valid @RequestBody TruckRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(truckService.updateTruck(id, request)));
    }

    @DeleteMapping("/trucks/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTruck(@PathVariable Long id) {
        truckService.deleteTruck(id);
        return ResponseEntity.ok(ApiResponse.ok("Truck deleted", null));
    }

    // Customers
    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCustomers(
            @RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(ApiResponse.ok(customerService.searchCustomers(search)));
        }
        return ResponseEntity.ok(ApiResponse.ok(customerService.getAllCustomers()));
    }

    @GetMapping("/customers/{id}/bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getCustomerBookings(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.getCustomerBookingHistory(id)));
    }

    // Calendar
    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>> getCalendar(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.ok(truckService.getAvailability(year, month)));
    }
}
