package com.chubb.BookingService.dto;

import java.util.List;

import com.chubb.BookingService.enums.Trip_Type;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateBookingRequest {

    @NotBlank(message = "Flight number cannot be empty")
    private String flightNumber;

    @NotNull(message = "Travel date is required")
    @FutureOrPresent(message = "Travel date cannot be in the past")
    private java.time.LocalDate travelDate;

    @NotNull(message = "Passengers list is required")
    @Size(min = 1, message = "At least one passenger is required")
    private List<PassengerRequest> passengers;

    @NotNull(message = "Trip type is required")
    private Trip_Type tripType;

    @NotNull(message = "Seats booked is required")
    @Positive(message = "Seats booked must be greater than 0")
    private Integer seatsBooked;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    private String contactEmail;
}