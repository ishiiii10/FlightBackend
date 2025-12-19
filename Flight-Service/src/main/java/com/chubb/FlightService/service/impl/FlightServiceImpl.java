package com.chubb.FlightService.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chubb.FlightService.dto.CreateFlightRequest;
import com.chubb.FlightService.dto.CreateFlightResponse;
import com.chubb.FlightService.dto.FlightSummaryResponse;
import com.chubb.FlightService.dto.InternalFlightResponse;
import com.chubb.FlightService.entity.Flight;
import com.chubb.FlightService.enums.City;
import com.chubb.FlightService.repository.FlightRepository;
import com.chubb.FlightService.service.FlightService;
import com.chubb.FlightService.service.SeatService;

@Service
@Transactional
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    private final SeatService seatService;

    public FlightServiceImpl(FlightRepository flightRepository, SeatService seatService) {
        this.flightRepository = flightRepository;
        this.seatService = seatService;
    }

    @Override
    public CreateFlightResponse createFlight(CreateFlightRequest request, String role) {

        // 🔐 Defensive role check (Gateway already enforces this)
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("Only ADMIN can create flights");
        }

        if (request.getArrivalTime().isBefore(request.getDepartureTime())) {
            throw new IllegalArgumentException("Arrival time cannot be before departure time");
        }

        if (request.getAvailableSeats() > request.getTotalSeats()) {
            throw new IllegalArgumentException("Available seats cannot be more than total seats");
        }

        Flight flight = Flight.builder()
                .flightNumber(request.getFlightNumber())
                .airline(request.getAirline())
                .source(request.getSource())
                .destination(request.getDestination())
                .departureTime(request.getDepartureTime())
                .arrivalTime(request.getArrivalTime())
                .totalSeats(request.getTotalSeats())
                .availableSeats(request.getAvailableSeats())
                .price(request.getPrice())
                .build();

        Flight saved = flightRepository.save(flight);

        return CreateFlightResponse.builder()
                .id(saved.getId())
                .message("Flight created successfully")
                .build();
    }

    @Override
    public List<FlightSummaryResponse> getAllFlights() {
        return flightRepository.findAll()
                .stream()
                .map(this::mapToSummary)
                .toList();
    }

    @Override
    public FlightSummaryResponse getFlightById(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found with ID: " + id));

        return mapToSummary(flight);
    }

    @Override
    public List<FlightSummaryResponse> searchFlights(City source, City destination) {

        if (source == destination) {
            throw new IllegalArgumentException("Source and destination cannot be the same");
        }

        return flightRepository.findBySourceAndDestination(source, destination)
                .stream()
                .map(this::mapToSummary)
                .toList();
    }

    @Override
    public InternalFlightResponse getInternalFlightDetails(String flightNumber) {

        Flight flight = flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found: " + flightNumber));

        return InternalFlightResponse.builder()
                .flightNumber(flight.getFlightNumber())
                .airline(flight.getAirline())
                .source(flight.getSource())
                .destination(flight.getDestination())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .totalSeats(flight.getTotalSeats())
                .availableSeats(flight.getAvailableSeats())
                .price(flight.getPrice())
                .build();
    }

    @Override
    public void updateFlight(Long id, CreateFlightRequest request, String role) {

        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("Only ADMIN can update flights");
        }

        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found with ID: " + id));

        if (request.getArrivalTime().isBefore(request.getDepartureTime())) {
            throw new IllegalArgumentException("Arrival time cannot be before departure time");
        }

        if (request.getAvailableSeats() > request.getTotalSeats()) {
            throw new IllegalArgumentException("Available seats cannot be more than total seats");
        }

        flight.setFlightNumber(request.getFlightNumber());
        flight.setAirline(request.getAirline());
        flight.setSource(request.getSource());
        flight.setDestination(request.getDestination());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        flight.setTotalSeats(request.getTotalSeats());
        flight.setAvailableSeats(request.getAvailableSeats());
        flight.setPrice(request.getPrice());

        flightRepository.save(flight);
    }

    @Override
    public void deleteFlight(Long id, String role) {
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("Only ADMIN can delete flights");
        }

        if (!flightRepository.existsById(id)) {
            throw new IllegalArgumentException("Flight not found with ID: " + id);
        }

        flightRepository.deleteById(id);
    }

    @Override
    public void reserveSeats(String flightNumber, int seatsToBook) {
        if (seatsToBook <= 0) {
            throw new IllegalArgumentException("Seats to book must be positive");
        }

        Flight flight = flightRepository.findByFlightNumberForUpdate(flightNumber)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found: " + flightNumber));

        int available = flight.getAvailableSeats();
        if (available < seatsToBook) {
            throw new IllegalStateException("Insufficient seats available");
        }

        flight.setAvailableSeats(available - seatsToBook);
        flightRepository.save(flight);
    }

    @Override
    public void releaseSeats(String flightNumber, int seatsToRelease) {
        if (seatsToRelease <= 0) {
            throw new IllegalArgumentException("Seats to release must be positive");
        }

        Flight flight = flightRepository.findByFlightNumberForUpdate(flightNumber)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found: " + flightNumber));

        int newAvailable = flight.getAvailableSeats() + seatsToRelease;
        if (newAvailable > flight.getTotalSeats()) {
            throw new IllegalStateException("Cannot release seats beyond total capacity");
        }

        flight.setAvailableSeats(newAvailable);
        flightRepository.save(flight);
    }

    @Override
    public List<String> getAvailableSeats(String flightNumber, String travelDate) {
        LocalDate date = LocalDate.parse(travelDate);
        
        // Check if seats are initialized, if not initialize them
        List<com.chubb.FlightService.entity.Seat> existingSeats = 
            seatService.getSeatsByFlightNumberAndTravelDate(flightNumber, date);
        
        if (existingSeats.isEmpty()) {
            // Seats not initialized, get flight details and initialize
            Flight flight = flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found: " + flightNumber));
            try {
                seatService.initializeSeatsForFlight(flightNumber, date, flight.getTotalSeats());
            } catch (Exception e) {
                // If initialization fails (e.g., seats already exist from concurrent request), 
                // continue and try to fetch available seats
                // This handles race conditions gracefully
            }
        }
        
        return seatService.getAvailableSeats(flightNumber, date);
    }

    @Override
    public void bookSeats(String flightNumber, String travelDate, List<String> seatNumbers, String bookingId) {
        LocalDate date = LocalDate.parse(travelDate);
        
        // Book seats in Seat table
        seatService.bookSeats(flightNumber, date, seatNumbers, bookingId);
        
        // Update Flight.availableSeats atomically
        Flight flight = flightRepository.findByFlightNumberForUpdate(flightNumber)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found: " + flightNumber));
        
        int seatsToBook = seatNumbers.size();
        int currentAvailable = flight.getAvailableSeats();
        
        if (currentAvailable < seatsToBook) {
            throw new IllegalStateException("Insufficient seats available. Available: " + currentAvailable + ", Requested: " + seatsToBook);
        }
        
        flight.setAvailableSeats(currentAvailable - seatsToBook);
        flightRepository.save(flight);
    }

    @Override
    public void releaseSeatsByBookingId(String bookingId) {
        // Get seats to be released to know which flight and how many
        List<com.chubb.FlightService.entity.Seat> seats = seatService.getSeatsByBookingId(bookingId);
        
        if (seats.isEmpty()) {
            return; // No seats to release
        }
        
        // Group by flight number and count seats per flight
        java.util.Map<String, Long> seatsPerFlight = seats.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        com.chubb.FlightService.entity.Seat::getFlightNumber,
                        java.util.stream.Collectors.counting()
                ));
        
        // Release seats in Seat table
        seatService.releaseSeats(bookingId);
        
        // Update Flight.availableSeats for each affected flight
        for (java.util.Map.Entry<String, Long> entry : seatsPerFlight.entrySet()) {
            String flightNumber = entry.getKey();
            int seatsToRelease = entry.getValue().intValue();
            
            Flight flight = flightRepository.findByFlightNumberForUpdate(flightNumber)
                    .orElseThrow(() -> new IllegalArgumentException("Flight not found: " + flightNumber));
            
            int currentAvailable = flight.getAvailableSeats();
            int newAvailable = currentAvailable + seatsToRelease;
            
            // Ensure we don't exceed total capacity
            if (newAvailable > flight.getTotalSeats()) {
                newAvailable = flight.getTotalSeats();
            }
            
            flight.setAvailableSeats(newAvailable);
            flightRepository.save(flight);
        }
    }

    @Override
    public void initializeSeats(String flightNumber, String travelDate, int totalSeats) {
        LocalDate date = LocalDate.parse(travelDate);
        seatService.initializeSeatsForFlight(flightNumber, date, totalSeats);
    }

    private FlightSummaryResponse mapToSummary(Flight flight) {
        return FlightSummaryResponse.builder()
                .id(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .airline(flight.getAirline())
                .source(flight.getSource())
                .destination(flight.getDestination())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .price(flight.getPrice())
                .availableSeats(flight.getAvailableSeats())
                .build();
    }
}