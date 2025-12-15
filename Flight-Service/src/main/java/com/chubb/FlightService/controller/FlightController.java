package com.chubb.FlightService.controller;

import com.chubb.FlightService.dto.CreateFlightRequest;
import com.chubb.FlightService.dto.CreateFlightResponse;
import com.chubb.FlightService.dto.FlightSummaryResponse;
import com.chubb.FlightService.dto.InternalFlightResponse;
import com.chubb.FlightService.enums.City;
import com.chubb.FlightService.service.FlightService;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}