package com.chubb.BookingService.entity;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.chubb.BookingService.enums.Booking_Status;
import com.chubb.BookingService.enums.Gender;
import com.chubb.BookingService.enums.Meal_Type;
import com.chubb.BookingService.enums.Trip_Type;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String pnr;

    @Column(nullable = false)
    private String flightNumber;

    @Column(nullable = false)
    private LocalDate travelDate;

    @Column(nullable = false)
    private String passengerName;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Meal_Type mealType;

    @Enumerated(EnumType.STRING)
    private Trip_Type tripType;

    @Column(nullable = false)
    private Integer seatsBooked;
    
    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Booking_Status status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    private String contactEmail;

    // Round trip fields
    private String returnFlightNumber;
    private LocalDate returnTravelDate;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Passenger> passengers;
}