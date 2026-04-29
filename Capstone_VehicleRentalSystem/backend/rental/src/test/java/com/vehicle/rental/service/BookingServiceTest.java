package com.vehicle.rental.service;

import com.vehicle.rental.dto.request.BookingRequest;
import com.vehicle.rental.dto.response.BookingResponse;
import com.vehicle.rental.entity.Booking;
import com.vehicle.rental.entity.Booking.BookingStatus;
import com.vehicle.rental.entity.User;
import com.vehicle.rental.entity.Vehicle;
import com.vehicle.rental.exception.BadRequestException;
import com.vehicle.rental.exception.ResourceNotFoundException;
import com.vehicle.rental.exception.UnauthorizedAccessException;
import com.vehicle.rental.exception.VehicleNotAvailableException;
import com.vehicle.rental.repository.BookingRepository;
import com.vehicle.rental.repository.UserRepository;
import com.vehicle.rental.repository.VehicleRepository;
import com.vehicle.rental.mapper.BookingMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingService bookingService;

    private Vehicle testVehicle;
    private User testUser;
    private BookingRequest testRequest;
    private Booking testBooking;
    private BookingResponse testResponse;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@driveeasy.com");

        testVehicle = new Vehicle();
        testVehicle.setId(100L);
        testVehicle.setName("Toyota Innova");
        testVehicle.setPricePerDay(3200.0);
        testVehicle.setActive(true);

        testRequest = new BookingRequest();
        testRequest.setVehicleId(100L);
        testRequest.setStartTime(LocalDateTime.now().plusDays(1));
        testRequest.setEndTime(LocalDateTime.now().plusDays(3));

        testBooking = new Booking();
        testBooking.setId(500L);
        testBooking.setVehicle(testVehicle);
        testBooking.setUser(testUser);
        testBooking.setTotalCost(6400.0);
        testBooking.setStatus(BookingStatus.PENDING);
        testBooking.setStartTime(LocalDateTime.now().plusDays(1));

        testResponse = new BookingResponse();
        testResponse.setId(500L);
        testResponse.setVehicleName("Toyota Innova");
        testResponse.setTotalCost(6400.0);
    }

    // =========================================================================
    // CREATE BOOKING TESTS
    // =========================================================================

    @Test
    void createBooking_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(vehicleRepository.findById(100L)).thenReturn(Optional.of(testVehicle));
        when(bookingRepository.isVehicleAvailable(eq(100L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(testResponse);

        BookingResponse actualResponse = bookingService.createBooking(1L, testRequest);

        assertNotNull(actualResponse);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void createBooking_EndTimeBeforeStartTime_ThrowsException() {
        testRequest.setEndTime(LocalDateTime.now().minusDays(1)); // Invalid dates

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(1L, testRequest));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_ExceedsMaximumDuration_ThrowsException() {
        testRequest.setEndTime(LocalDateTime.now().plusDays(35)); // > 30 days

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(1L, testRequest));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_VehicleInactive_ThrowsException() {
        testVehicle.setActive(false);
        when(vehicleRepository.findById(100L)).thenReturn(Optional.of(testVehicle));

        assertThrows(BadRequestException.class, () -> bookingService.createBooking(1L, testRequest));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_VehicleAlreadyBooked_ThrowsException() {
        when(vehicleRepository.findById(100L)).thenReturn(Optional.of(testVehicle));
        when(bookingRepository.isVehicleAvailable(eq(100L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);

        assertThrows(VehicleNotAvailableException.class, () -> bookingService.createBooking(1L, testRequest));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_VehicleNotFound_ThrowsException() {
        when(vehicleRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookingService.createBooking(1L, testRequest));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_UserNotFound_ThrowsException() {
        when(vehicleRepository.findById(100L)).thenReturn(Optional.of(testVehicle));
        when(bookingRepository.isVehicleAvailable(eq(100L), any(), any())).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookingService.createBooking(1L, testRequest));
    }

    // =========================================================================
    // CONFIRM BOOKING TESTS
    // =========================================================================

    @Test
    void confirmBooking_Success() {
        when(bookingRepository.findById(500L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(testResponse);

        bookingService.confirmBooking(500L, 1L);

        assertEquals(BookingStatus.ACTIVE, testBooking.getStatus());
        verify(bookingRepository, times(1)).save(testBooking);
    }

    @Test
    void confirmBooking_UnauthorizedUser_ThrowsException() {
        when(bookingRepository.findById(500L)).thenReturn(Optional.of(testBooking));

        // Attempting to confirm with User ID 2 instead of 1
        assertThrows(UnauthorizedAccessException.class, () -> bookingService.confirmBooking(500L, 2L));
    }

    @Test
    void confirmBooking_NotPending_ThrowsException() {
        testBooking.setStatus(BookingStatus.ACTIVE); // Already active
        when(bookingRepository.findById(500L)).thenReturn(Optional.of(testBooking));

        assertThrows(BadRequestException.class, () -> bookingService.confirmBooking(500L, 1L));
    }

    // =========================================================================
    // CANCEL BOOKING TESTS
    // =========================================================================

    @Test
    void cancelBooking_Success() {
        when(bookingRepository.findById(500L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(testResponse);

        bookingService.cancelBooking(500L, 1L);

        assertEquals(BookingStatus.CANCELLED, testBooking.getStatus());
        verify(bookingRepository, times(1)).save(testBooking);
    }

    @Test
    void cancelBooking_TooLate_ThrowsException() {
        // Set the start time to yesterday
        testBooking.setStartTime(LocalDateTime.now().minusDays(1));
        when(bookingRepository.findById(500L)).thenReturn(Optional.of(testBooking));

        assertThrows(BadRequestException.class, () -> bookingService.cancelBooking(500L, 1L));
        assertNotEquals(BookingStatus.CANCELLED, testBooking.getStatus());
    }

    // =========================================================================
    // PAGINATION TESTS
    // =========================================================================

    @Test
    void getUserBookings_Success() {
        Page<Booking> page = new PageImpl<>(List.of(testBooking));
        when(bookingRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(page);
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(testResponse);

        Page<BookingResponse> result = bookingService.getUserBookings(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(bookingRepository, times(1)).findByUserId(eq(1L), any(Pageable.class));
    }

    @Test
    void getAllBookings_Success() {
        Page<Booking> page = new PageImpl<>(List.of(testBooking));
        when(bookingRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(testResponse);

        Page<BookingResponse> result = bookingService.getAllBookings(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(bookingRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void getBookingsByStatus_Success() {
        Page<Booking> page = new PageImpl<>(List.of(testBooking));
        when(bookingRepository.findByStatus(eq(BookingStatus.PENDING), any(Pageable.class))).thenReturn(page);
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(testResponse);

        Page<BookingResponse> result = bookingService.getBookingsByStatus(BookingStatus.PENDING, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(bookingRepository, times(1)).findByStatus(eq(BookingStatus.PENDING), any(Pageable.class));
    }
}