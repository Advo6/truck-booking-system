package com.truckbooking.repository;

import com.truckbooking.model.Booking;
import com.truckbooking.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @Query("SELECT b FROM Booking b WHERE b.status IN :statuses " +
           "AND b.pickupDate <= :endDate AND b.returnDate >= :startDate")
    List<Booking> findOverlappingBookings(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<BookingStatus> statuses);

    @Query("SELECT b FROM Booking b WHERE b.truck.id = :truckId AND b.status IN :statuses " +
           "AND b.pickupDate <= :endDate AND b.returnDate >= :startDate")
    List<Booking> findOverlappingBookingsForTruck(
            @Param("truckId") Long truckId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<BookingStatus> statuses);

    @Query("SELECT b FROM Booking b WHERE b.pickupDate = :date OR " +
           "(b.pickupDate <= :date AND b.returnDate >= :date)")
    List<Booking> findBookingsForDate(@Param("date") LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.status IN :statuses " +
           "AND b.pickupDate <= :monthEnd AND b.returnDate >= :monthStart")
    List<Booking> findBookingsInRange(
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd") LocalDate monthEnd,
            @Param("statuses") List<BookingStatus> statuses);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.pickupDate = :today")
    long countTodayBookings(@Param("today") LocalDate today);

    @Query("SELECT b FROM Booking b WHERE " +
           "LOWER(b.bookingReference) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.customer.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.customer.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Booking> searchBookings(@Param("query") String query);

    List<Booking> findByStatus(BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.pickupDate <= :dateTo AND b.returnDate >= :dateFrom ORDER BY b.createdAt DESC")
    List<Booking> findByDateRange(@Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);

    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.pickupDate <= :dateTo AND b.returnDate >= :dateFrom ORDER BY b.createdAt DESC")
    List<Booking> findByStatusAndDateRange(
            @Param("status") BookingStatus status,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.status IN :statuses")
    BigDecimal sumRevenueByStatuses(@Param("statuses") List<BookingStatus> statuses);
}
