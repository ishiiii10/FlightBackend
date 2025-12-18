package com.chubb.FlightService.service;

import java.util.List;

import com.chubb.FlightService.dto.CreateFlightRequest;
import com.chubb.FlightService.dto.CreateFlightResponse;
import com.chubb.FlightService.dto.FlightSummaryResponse;
import com.chubb.FlightService.dto.InternalFlightResponse;
import com.chubb.FlightService.enums.City;

public interface FlightService {

    CreateFlightResponse createFlight(CreateFlightRequest request, String role);

    List<FlightSummaryResponse> getAllFlights();

    FlightSummaryResponse getFlightById(Long id);

    List<FlightSummaryResponse> searchFlights(City source, City destination);

    InternalFlightResponse getInternalFlightDetails(String flightNumber);

    void updateFlight(Long id, CreateFlightRequest request, String role);

    void deleteFlight(Long id, String role);

    /**
     * Atomically reserve seats for a given flight.
     */
    void reserveSeats(String flightNumber, int seatsToBook);

    /**
     * Atomically release seats (on booking cancellation).
     */
    void releaseSeats(String flightNumber, int seatsToRelease);

    // Seat management methods
    List<String> getAvailableSeats(String flightNumber, String travelDate);
    void bookSeats(String flightNumber, String travelDate, List<String> seatNumbers, String bookingId);
    void releaseSeatsByBookingId(String bookingId);
    void initializeSeats(String flightNumber, String travelDate, int totalSeats);
}