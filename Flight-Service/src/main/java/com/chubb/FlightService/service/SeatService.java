package com.chubb.FlightService.service;

import java.time.LocalDate;
import java.util.List;

public interface SeatService {
    
    /**
     * Initialize seats for a flight on a given date
     * Creates seats like 1A, 1B, 1C, 2A, 2B, 2C, etc.
     */
    void initializeSeatsForFlight(String flightNumber, LocalDate travelDate, int totalSeats);
    
    /**
     * Get available seats for a flight on a given date
     */
    List<String> getAvailableSeats(String flightNumber, LocalDate travelDate);
    
    /**
     * Book specific seats (atomically)
     * @return true if all seats were successfully booked, false otherwise
     */
    boolean bookSeats(String flightNumber, LocalDate travelDate, List<String> seatNumbers, String bookingId);
    
    /**
     * Release seats back to available (on cancellation)
     */
    void releaseSeats(String bookingId);
    
    /**
     * Check if seats are available
     */
    boolean areSeatsAvailable(String flightNumber, LocalDate travelDate, List<String> seatNumbers);
    
    /**
     * Get seats by booking ID (for cancellation)
     */
    List<com.chubb.FlightService.entity.Seat> getSeatsByBookingId(String bookingId);
    
    /**
     * Get all seats for a flight on a given date (for checking if initialized)
     */
    List<com.chubb.FlightService.entity.Seat> getSeatsByFlightNumberAndTravelDate(String flightNumber, LocalDate travelDate);
}

