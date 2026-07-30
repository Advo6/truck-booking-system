package com.truckbooking.service;

import com.truckbooking.dto.BookingRequest;
import com.truckbooking.dto.BookingResponse;
import com.truckbooking.exception.BookingConflictException;
import com.truckbooking.exception.ResourceNotFoundException;
import com.truckbooking.model.Booking;
import com.truckbooking.model.Customer;
import com.truckbooking.model.Truck;
import com.truckbooking.model.enums.BookingStatus;
import com.truckbooking.model.enums.TruckStatus;
import com.truckbooking.repository.BookingRepository;
import com.truckbooking.repository.CustomerRepository;
import com.truckbooking.repository.TruckRepository;
import com.truckbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final List<BookingStatus> ACTIVE_STATUSES = Arrays.asList(
            BookingStatus.PENDING, BookingStatus.APPROVED
    );

    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;
    private final TruckRepository truckRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.booking.daily-rate:3500.00}")
    private BigDecimal dailyRate;

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        validateDates(request.getPickupDate(), request.getReturnDate());

        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseGet(() -> customerRepository.save(Customer.builder()
                        .name(request.getFullName())
                        .phone(request.getPhone())
                        .email(request.getEmail())
                        .build()));

        // Update customer details if they already exist
        customer.setName(request.getFullName());
        customer.setPhone(request.getPhone());
        customerRepository.save(customer);

        Truck truck = resolveTruck(request);

        if (hasOverlap(truck.getId(), request.getPickupDate(), request.getReturnDate(), null)) {
            throw new BookingConflictException(
                    "Truck " + truck.getTruckNumber() + " is not available for the selected dates."
            );
        }

        Booking booking = Booking.builder()
                .customer(customer)
                .truck(truck)
                .pickupDate(request.getPickupDate())
                .returnDate(request.getReturnDate())
                .pickupAddress(request.getPickupAddress())
                .deliveryAddress(request.getDeliveryAddress())
                .cargoDescription(request.getCargoDescription())
                .weight(request.getWeight())
                .specialInstructions(request.getSpecialInstructions())
                .status(BookingStatus.PENDING)
                .bookingReference(generateReference())
                .totalAmount(calculateAmount(request.getPickupDate(), request.getReturnDate()))
                .build();

        booking = bookingRepository.save(booking);
        updateTruckStatus(truck);

        emailService.sendBookingConfirmation(booking);

        return toResponse(booking);
    }

    /** Resolves truck: use requested truck or auto-suggest alternate if unavailable. */
    private Truck resolveTruck(BookingRequest request) {
        List<Truck> trucks = truckRepository.findAll();

        if (request.getTruckId() != null) {
            Truck requested = truckRepository.findById(request.getTruckId())
                    .orElseThrow(() -> new ResourceNotFoundException("Truck not found"));

            if (requested.getStatus() == TruckStatus.MAINTENANCE) {
                throw new BookingConflictException("Selected truck is under maintenance.");
            }

            if (!hasOverlap(requested.getId(), request.getPickupDate(), request.getReturnDate(), null)) {
                return requested;
            }

            // Try alternate truck
            Truck alternate = trucks.stream()
                    .filter(t -> !t.getId().equals(requested.getId()))
                    .filter(t -> t.getStatus() != TruckStatus.MAINTENANCE)
                    .filter(t -> !hasOverlap(t.getId(), request.getPickupDate(), request.getReturnDate(), null))
                    .findFirst()
                    .orElseThrow(() -> new BookingConflictException(
                            "No trucks available for the selected dates."
                    ));

            return alternate;
        }

        // No truck specified — pick first available
        return trucks.stream()
                .filter(t -> t.getStatus() != TruckStatus.MAINTENANCE)
                .filter(t -> !hasOverlap(t.getId(), request.getPickupDate(), request.getReturnDate(), null))
                .findFirst()
                .orElseThrow(() -> new BookingConflictException(
                        "No trucks available for the selected dates."
                ));
    }

    public boolean hasOverlap(Long truckId, LocalDate start, LocalDate end, Long excludeBookingId) {
        List<Booking> overlapping = bookingRepository.findOverlappingBookingsForTruck(
                truckId, start, end, ACTIVE_STATUSES);

        if (excludeBookingId != null) {
            overlapping = overlapping.stream()
                    .filter(b -> !b.getId().equals(excludeBookingId))
                    .collect(Collectors.toList());
        }

        return !overlapping.isEmpty();
    }

    private void validateDates(LocalDate pickup, LocalDate returnDate) {
        if (pickup.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Pickup date cannot be in the past.");
        }
        if (!returnDate.isAfter(pickup)) {
            throw new IllegalArgumentException("Return date must be after pickup date.");
        }
    }

    private String generateReference() {
        return "TB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public BookingResponse getByReference(String reference) {
        Booking booking = bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        return toResponse(booking);
    }

    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<BookingResponse> getCustomerBookings(Long customerId) {
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<BookingResponse> searchBookings(String query) {
        return bookingRepository.searchBookings(query).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<BookingResponse> filterByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<BookingResponse> filterBookings(String search, BookingStatus status,
                                                   LocalDate dateFrom, LocalDate dateTo) {
        List<Booking> bookings;

        if (search != null && !search.isBlank()) {
            bookings = bookingRepository.searchBookings(search);
        } else if (status != null && dateFrom != null && dateTo != null) {
            bookings = bookingRepository.findByStatusAndDateRange(status, dateFrom, dateTo);
        } else if (status != null) {
            bookings = bookingRepository.findByStatus(status);
        } else if (dateFrom != null && dateTo != null) {
            bookings = bookingRepository.findByDateRange(dateFrom, dateTo);
        } else {
            bookings = bookingRepository.findAll();
        }

        return bookings.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<BookingResponse> getBookingsByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No bookings found for this email"));
        return getCustomerBookings(customer.getId());
    }

    public List<BookingResponse> getMyBookings(String username) {
        return userRepository.findByUsername(username)
                .flatMap(u -> customerRepository.findByUserId(u.getId()))
                .map(c -> getCustomerBookings(c.getId()))
                .orElse(List.of());
    }

    private BigDecimal calculateAmount(LocalDate pickup, LocalDate returnDate) {
        long days = Math.max(1, ChronoUnit.DAYS.between(pickup, returnDate));
        return dailyRate.multiply(BigDecimal.valueOf(days));
    }

    @Transactional
    public BookingResponse updateStatus(Long id, BookingStatus status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        booking.setStatus(status);
        booking = bookingRepository.save(booking);
        updateTruckStatus(booking.getTruck());

        return toResponse(booking);
    }

    @Transactional
    public BookingResponse updateBooking(Long id, BookingRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        validateDates(request.getPickupDate(), request.getReturnDate());

        Long truckId = request.getTruckId() != null ? request.getTruckId() : booking.getTruck().getId();

        if (hasOverlap(truckId, request.getPickupDate(), request.getReturnDate(), id)) {
            throw new BookingConflictException("Truck is not available for the selected dates.");
        }

        Truck truck = truckRepository.findById(truckId)
                .orElseThrow(() -> new ResourceNotFoundException("Truck not found"));

        booking.setTruck(truck);
        booking.setPickupDate(request.getPickupDate());
        booking.setReturnDate(request.getReturnDate());
        booking.setPickupAddress(request.getPickupAddress());
        booking.setDeliveryAddress(request.getDeliveryAddress());
        booking.setCargoDescription(request.getCargoDescription());
        booking.setWeight(request.getWeight());
        booking.setSpecialInstructions(request.getSpecialInstructions());
        booking.setTotalAmount(calculateAmount(request.getPickupDate(), request.getReturnDate()));

        booking = bookingRepository.save(booking);
        truckRepository.findAll().forEach(this::updateTruckStatus);

        return toResponse(booking);
    }

    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        Truck truck = booking.getTruck();
        bookingRepository.delete(booking);
        updateTruckStatus(truck);
    }

    /** Release trucks when bookings expire (scheduled daily). */
    @Transactional
    public void releaseExpiredBookings() {
        LocalDate today = LocalDate.now();
        bookingRepository.findAll().stream()
                .filter(b -> b.getReturnDate().isBefore(today))
                .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                .forEach(b -> {
                    b.setStatus(BookingStatus.COMPLETED);
                    bookingRepository.save(b);
                });
        truckRepository.findAll().forEach(this::updateTruckStatus);
    }

    private void updateTruckStatus(Truck truck) {
        LocalDate today = LocalDate.now();
        boolean hasActiveBooking = bookingRepository.findOverlappingBookingsForTruck(
                truck.getId(), today, today.plusYears(1), ACTIVE_STATUSES
        ).stream().anyMatch(b -> !b.getReturnDate().isBefore(today));

        if (truck.getStatus() != TruckStatus.MAINTENANCE) {
            truck.setStatus(hasActiveBooking ? TruckStatus.BOOKED : TruckStatus.AVAILABLE);
            truckRepository.save(truck);
        }
    }

    public BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .customerName(booking.getCustomer().getName())
                .customerPhone(booking.getCustomer().getPhone())
                .customerEmail(booking.getCustomer().getEmail())
                .truckId(booking.getTruck().getId())
                .truckNumber(booking.getTruck().getTruckNumber())
                .truckRegistration(booking.getTruck().getRegistration())
                .pickupDate(booking.getPickupDate())
                .returnDate(booking.getReturnDate())
                .pickupAddress(booking.getPickupAddress())
                .deliveryAddress(booking.getDeliveryAddress())
                .cargoDescription(booking.getCargoDescription())
                .weight(booking.getWeight())
                .specialInstructions(booking.getSpecialInstructions())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .totalAmount(booking.getTotalAmount())
                .build();
    }
}
