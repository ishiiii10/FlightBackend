package com.chubb.FlightService.dto;

import com.chubb.FlightService.enums.Airline;
import com.chubb.FlightService.enums.City;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class InternalFlightResponse {

    private String flightNumber;
    private Airline airline;
    private City source;
    private City destination;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer totalSeats;
    private Integer availableSeats;
    private BigDecimal price;
}