package com.chubb.FlightService.service.impl;

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

@Service
@Transactional
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;

    public FlightServiceImpl(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
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