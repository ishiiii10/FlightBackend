package com.chubb.BookingService.dto;

import com.chubb.BookingService.enums.Gender;
import com.chubb.BookingService.enums.Meal_Type;
import com.chubb.BookingService.enums.Trip_Type;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateBookingRequest {

    @NotBlank(message = "Flight number cannot be empty")
    private String flightNumber;

    @NotNull(message = "Travel date is required")
    @FutureOrPresent(message = "Travel date cannot be in the past")
    private java.time.LocalDate travelDate;

    @NotBlank(message = "Passenger name cannot be empty")
    private String passengerName;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Meal type is required")
    private Meal_Type mealType;

    @NotNull(message = "Trip type is required")
    private Trip_Type tripType;

    @NotNull(message = "Seats booked is required")
    @Positive(message = "Seats booked must be greater than 0")
    private Integer seatsBooked;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    private String contactEmail;
}