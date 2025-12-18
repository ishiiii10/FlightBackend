package com.chubb.BookingService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.chubb.BookingService.dto.BookSeatsRequest;
import com.chubb.BookingService.dto.FlightDetailsResponse;

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

    // Seat management endpoints
    @GetMapping("/flights/internal/{flightNumber}/seats/available")
    java.util.List<String> getAvailableSeats(
            @PathVariable String flightNumber,
            @RequestParam("travelDate") String travelDate
    );

    @PostMapping("/flights/internal/{flightNumber}/seats/book")
    void bookSeats(
            @PathVariable String flightNumber,
            @RequestBody BookSeatsRequest request
    );

    @PostMapping("/flights/internal/seats/release")
    void releaseSeatsByBookingId(@RequestParam("bookingId") String bookingId);

    @PostMapping("/flights/internal/{flightNumber}/seats/initialize")
    void initializeSeats(
            @PathVariable String flightNumber,
            @RequestParam("travelDate") String travelDate,
            @RequestParam("totalSeats") int totalSeats
    );
}