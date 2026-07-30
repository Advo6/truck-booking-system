package com.truckbooking.service;

import com.truckbooking.dto.BookingResponse;
import com.truckbooking.model.Customer;
import com.truckbooking.repository.BookingRepository;
import com.truckbooking.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    public List<Map<String, Object>> getAllCustomers() {
        return customerRepository.findAll().stream().map(this::toMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> searchCustomers(String query) {
        return customerRepository.search(query).stream().map(this::toMap).collect(Collectors.toList());
    }

    public List<BookingResponse> getCustomerBookingHistory(Long customerId) {
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(bookingService::toResponse)
                .collect(Collectors.toList());
    }

    private Map<String, Object> toMap(Customer customer) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", customer.getId());
        map.put("name", customer.getName());
        map.put("phone", customer.getPhone());
        map.put("email", customer.getEmail());
        map.put("bookingCount", customer.getBookings() != null ? customer.getBookings().size() : 0);
        return map;
    }
}
