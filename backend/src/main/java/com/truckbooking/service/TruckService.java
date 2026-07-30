package com.truckbooking.service;

import com.truckbooking.dto.AvailabilityResponse;
import com.truckbooking.dto.TruckRequest;
import com.truckbooking.dto.TruckResponse;
import com.truckbooking.exception.ResourceNotFoundException;
import com.truckbooking.model.Booking;
import com.truckbooking.model.Truck;
import com.truckbooking.model.enums.BookingStatus;
import com.truckbooking.model.enums.TruckStatus;
import com.truckbooking.repository.BookingRepository;
import com.truckbooking.repository.TruckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TruckService {

    private static final List<BookingStatus> ACTIVE_STATUSES = Arrays.asList(
            BookingStatus.PENDING, BookingStatus.APPROVED
    );

    private final TruckRepository truckRepository;
    private final BookingRepository bookingRepository;

    public List<TruckResponse> getAllTrucks() {
        return truckRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TruckResponse getTruckById(Long id) {
        Truck truck = truckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Truck not found"));
        return toResponse(truck);
    }

    @Transactional
    public TruckResponse createTruck(TruckRequest request) {
        Truck truck = Truck.builder()
                .truckNumber(request.getTruckNumber())
                .registration(request.getRegistration())
                .capacity(request.getCapacity())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TruckStatus.AVAILABLE)
                .image(request.getImage())
                .build();
        return toResponse(truckRepository.save(truck));
    }

    @Transactional
    public TruckResponse updateTruck(Long id, TruckRequest request) {
        Truck truck = truckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Truck not found"));

        truck.setTruckNumber(request.getTruckNumber());
        truck.setRegistration(request.getRegistration());
        truck.setCapacity(request.getCapacity());
        truck.setDescription(request.getDescription());
        if (request.getStatus() != null) truck.setStatus(request.getStatus());
        if (request.getImage() != null) truck.setImage(request.getImage());

        return toResponse(truckRepository.save(truck));
    }

    @Transactional
    public void deleteTruck(Long id) {
        if (!truckRepository.existsById(id)) {
            throw new ResourceNotFoundException("Truck not found");
        }
        truckRepository.deleteById(id);
    }

    public List<AvailabilityResponse> getAvailability(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        List<Booking> bookings = bookingRepository.findBookingsInRange(monthStart, monthEnd, ACTIVE_STATUSES);

        return truckRepository.findAll().stream().map(truck -> {
            Map<String, String> dateStatus = new LinkedHashMap<>();
            List<String> bookedDates = new ArrayList<>();

            for (LocalDate date = monthStart; !date.isAfter(monthEnd); date = date.plusDays(1)) {
                String dateStr = date.toString();
                if (truck.getStatus() == TruckStatus.MAINTENANCE) {
                    dateStatus.put(dateStr, "maintenance");
                } else {
                    String status = resolveDateStatus(truck.getId(), date, bookings);
                    dateStatus.put(dateStr, status);
                    if ("booked".equals(status) || "pending".equals(status)) {
                        bookedDates.add(dateStr);
                    }
                }
            }

            boolean available = truck.getStatus() == TruckStatus.AVAILABLE
                    && bookedDates.size() < monthEnd.getDayOfMonth();

            return AvailabilityResponse.builder()
                    .truckId(truck.getId())
                    .truckNumber(truck.getTruckNumber())
                    .available(available)
                    .bookedDates(bookedDates)
                    .dateStatus(dateStatus)
                    .build();
        }).collect(Collectors.toList());
    }

    private String resolveDateStatus(Long truckId, LocalDate date, List<Booking> bookings) {
        for (Booking booking : bookings) {
            if (!booking.getTruck().getId().equals(truckId)) continue;
            if (!date.isBefore(booking.getPickupDate()) && !date.isAfter(booking.getReturnDate())) {
                return booking.getStatus() == BookingStatus.PENDING ? "pending" : "booked";
            }
        }
        return "available";
    }

    public TruckResponse toResponse(Truck truck) {
        return TruckResponse.builder()
                .id(truck.getId())
                .truckNumber(truck.getTruckNumber())
                .registration(truck.getRegistration())
                .capacity(truck.getCapacity())
                .description(truck.getDescription())
                .status(truck.getStatus())
                .image(truck.getImage())
                .build();
    }
}
