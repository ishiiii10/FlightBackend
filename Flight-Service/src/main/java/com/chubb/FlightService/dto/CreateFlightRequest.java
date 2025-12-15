package com.chubb.FlightService.dto;

import com.chubb.FlightService.enums.Airline;
import com.chubb.FlightService.enums.City;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateFlightRequest {

    @NotBlank(message = "Flight number cannot be empty")
    private String flightNumber;

    @NotNull(message = "Airline is required")
    private Airline airline;

    @NotNull(message = "Source city is required")
    private City source;

    @NotNull(message = "Destination city is required")
    private City destination;

    @NotNull(message = "Departure time is required")
    @FutureOrPresent(message = "Departure time cannot be in the past")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time is required")
    @Future(message = "Arrival time must be a future time")
    private LocalDateTime arrivalTime;

    @NotNull(message = "Total seats is required")
    @Positive(message = "Total seats must be > 0")
    private Integer totalSeats;

    @NotNull(message = "Available seats is required")
    @PositiveOrZero(message = "Available seats cannot be negative")
    private Integer availableSeats;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    private BigDecimal price;
}