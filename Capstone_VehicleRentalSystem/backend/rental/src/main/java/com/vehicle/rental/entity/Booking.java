package com.vehicle.rental.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;


// It connects a User to a specific Vehicle for a specific period. By making this a dedicated entity
// rather than a simple join table, we can track vital business data like 
// the final price and current lifecycle status.
@Data
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // We link the User here using @ManyToOne. I used @JsonIgnoreProperties
    // because while the frontend needs to know 'who' booked the car (name/email),
    // it should never receive the user's hashed password or their list of other 
    // bookings, which would cause a circular reference crash.
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"bookings", "password", "role"})
    private User user;

    // Standard relationship to the Vehicle. We ignore the 'isActive' flag and
    // the vehicle's own booking history here to keep the JSON response clean 
    // and focused strictly on the car's details (Name, Model, Type).
    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonIgnoreProperties({"bookings", "isActive"})
    private Vehicle vehicle;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // We 'snapshot' the total  price here (Days * PricePerDay) at the exact moment of creation.
    // This ensures that if the Admin raises rental prices next month, 
    // the user's historical receipts remain accurate and unchanged.
    @Column(name = "total_cost", nullable = false)
    private Double totalCost;

    // Every booking starts as PENDING. This allows the user to see the 
    // calculated summary on the frontend before they 'Confirm' the rental.
    // It's the foundation for  mock payment/confirmation flow.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // I designed this Enum to support a full historical audit trail. 
    // Even after a trip is finished or cancelled, we keep the record 
    // so the user can see their 'Booking History' in their dashboard.
    public enum BookingStatus {
        PENDING,   // Created but waiting for user confirmation
        ACTIVE,    // Confirmed and currently in progress
        CANCELLED, // Terminated by user (retains financial record)
        COMPLETED  // Trip finished successfully; vehicle is now free
    }
}