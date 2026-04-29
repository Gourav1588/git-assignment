package com.vehicle.rental.mapper;

import com.vehicle.rental.dto.response.BookingResponse;
import com.vehicle.rental.entity.Booking;
import com.vehicle.rental.entity.Booking.BookingStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Mapper component for translating Booking entities into BookingResponse DTOs.
 */
@Component
public class BookingMapper {

    /**
     * Transforms complex nested Booking entity data into a flattened, client-friendly format.
     * Dynamically overrides 'ACTIVE' status to 'COMPLETED' if the end time has passed.
     */
    public BookingResponse toResponse(Booking booking) {
        BookingResponse response = new BookingResponse();

        response.setId(booking.getId());

        // Flatten vehicle relationships
        response.setVehicleId(booking.getVehicle().getId());
        response.setVehicleName(booking.getVehicle().getName());
        response.setVehicleType(booking.getVehicle().getType().name());
        response.setPricePerDay(booking.getVehicle().getPricePerDay());

        response.setStartTime(booking.getStartTime());
        response.setEndTime(booking.getEndTime());

        // Calculate total inclusive hours
        long hours = ChronoUnit.HOURS.between(booking.getStartTime(), booking.getEndTime());
        response.setTotalHours(hours < 1 ? 1 : hours);

        response.setTotalCost(booking.getTotalCost());

        /* =========================================================================
           DYNAMIC STATUS RESOLUTION (On-The-Fly)
           Checks the exact current hour/minute against the trip's end time.
           ========================================================================= */
        if (booking.getStatus() == BookingStatus.ACTIVE && LocalDateTime.now().isAfter(booking.getEndTime())) {
            response.setStatus(BookingStatus.COMPLETED);
        } else {
            response.setStatus(booking.getStatus());
        }

        // Flatten user relationships
        response.setUserId(booking.getUser().getId());
        response.setUserName(booking.getUser().getName());

        response.setCreatedAt(booking.getCreatedAt());

        return response;
    }
}