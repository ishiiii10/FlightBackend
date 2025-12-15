package com.chubb.BookingService.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.chubb.BookingService.enums.Booking_Status;
import com.chubb.BookingService.enums.Gender;
import com.chubb.BookingService.enums.Meal_Type;
import com.chubb.BookingService.enums.Trip_Type;

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
}