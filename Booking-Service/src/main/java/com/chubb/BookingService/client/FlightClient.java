package com.chubb.BookingService.client;

import com.chubb.BookingService.dto.FlightDetailsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "FLIGHTSERVICE")
public interface FlightClient {

    @GetMapping("/flights/internal/{flightNumber}")
    FlightDetailsResponse getFlightDetails(@PathVariable String flightNumber);

    @PostMapping("/flights/internal/{flightNumber}/reserve")
    void reserveSeats(
            @PathVariable String flightNumber,
            @RequestParam("seats") int seats
    );

    @PostMapping("/flights/internal/{flightNumber}/release")
    void releaseSeats(
            @PathVariable String flightNumber,
            @RequestParam("seats") int seats
    );
}