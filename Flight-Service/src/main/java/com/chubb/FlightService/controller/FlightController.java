package com.chubb.FlightService.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chubb.FlightService.dto.CreateFlightRequest;
import com.chubb.FlightService.dto.CreateFlightResponse;
import com.chubb.FlightService.dto.FlightSummaryResponse;
import com.chubb.FlightService.dto.InternalFlightResponse;
import com.chubb.FlightService.enums.City;
import com.chubb.FlightService.service.FlightService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/flights")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    // 🔐 ADMIN ONLY (Gateway enforces)
    @PostMapping
    public ResponseEntity<CreateFlightResponse> createFlight(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody CreateFlightRequest request) {

        CreateFlightResponse response =
                flightService.createFlight(request, role);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 🔐 ADMIN ONLY – update flight
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateFlight(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id,
            @Valid @RequestBody CreateFlightRequest request
    ) {
        flightService.updateFlight(id, request, role);
        return ResponseEntity.noContent().build();
    }

    // 🔐 ADMIN ONLY – delete flight
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlight(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id
    ) {
        flightService.deleteFlight(id, role);
        return ResponseEntity.noContent().build();
    }

    // 🌍 PUBLIC
    @GetMapping
    public ResponseEntity<List<FlightSummaryResponse>> getAllFlights() {
        return ResponseEntity.ok(flightService.getAllFlights());
    }

    // 🌍 PUBLIC
    @GetMapping("/{id}")
    public ResponseEntity<FlightSummaryResponse> getFlightById(@PathVariable Long id) {
        return ResponseEntity.ok(flightService.getFlightById(id));
    }

    // 🌍 PUBLIC
    @GetMapping("/search")
    public ResponseEntity<List<FlightSummaryResponse>> searchFlights(
            @RequestParam City source,
            @RequestParam City destination) {

        return ResponseEntity.ok(
                flightService.searchFlights(source, destination)
        );
    }

    // 🔒 INTERNAL (only Booking Service via Gateway)
    @GetMapping("/internal/{flightNumber}")
    public ResponseEntity<InternalFlightResponse> getInternalFlight(
            @PathVariable String flightNumber) {

        return ResponseEntity.ok(
                flightService.getInternalFlightDetails(flightNumber)
        );
    }

    // 🔒 INTERNAL – atomic seat reservation
    @PostMapping("/internal/{flightNumber}/reserve")
    public ResponseEntity<Void> reserveSeats(
            @PathVariable String flightNumber,
            @RequestParam("seats") int seats
    ) {
        flightService.reserveSeats(flightNumber, seats);
        return ResponseEntity.ok().build();
    }

    // 🔒 INTERNAL – release seats on cancellation
    @PostMapping("/internal/{flightNumber}/release")
    public ResponseEntity<Void> releaseSeats(
            @PathVariable String flightNumber,
            @RequestParam("seats") int seats
    ) {
        flightService.releaseSeats(flightNumber, seats);
        return ResponseEntity.ok().build();
    }
}