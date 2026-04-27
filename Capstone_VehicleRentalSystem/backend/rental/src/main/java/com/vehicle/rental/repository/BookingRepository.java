package com.vehicle.rental.repository;

import com.vehicle.rental.entity.Booking;
import com.vehicle.rental.entity.Booking.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository // Marks as JPA repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Get bookings by user (paginated)
    Page<Booking> findByUserId(Long userId, Pageable pageable);

    // Get bookings by status (paginated)
    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    // Check if vehicle has bookings with given statuses
    boolean existsByVehicleIdAndStatusIn(Long vehicleId, List<BookingStatus> statuses);

    // Check vehicle availability for date range
    @Query("""
        SELECT COUNT(b) = 0 
        FROM Booking b 
        WHERE b.vehicle.id = :vehicleId 
        AND b.status IN ('PENDING', 'ACTIVE') 
        AND b.startDate <= :endDate 
        AND b.endDate >= :startDate
        """)
    boolean isVehicleAvailable(
            @Param("vehicleId") Long vehicleId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Check active or future bookings for vehicle
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.vehicle.id = :vehicleId " +
            "AND b.status IN ('ACTIVE', 'CONFIRMED', 'PENDING') " +
            "AND b.endDate >= CURRENT_DATE")
    boolean existsActiveOrFutureBookingsForVehicle(@Param("vehicleId") Long vehicleId);

    // Mark past bookings as completed
    @Modifying
    @Transactional
    @Query("UPDATE Booking b SET b.status = 'COMPLETED' " +
            "WHERE b.status = 'ACTIVE' AND b.endDate < :today")
    int completePastBookings(@Param("today") LocalDate today);
}