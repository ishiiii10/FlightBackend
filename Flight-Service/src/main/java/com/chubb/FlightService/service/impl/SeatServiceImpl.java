package com.chubb.FlightService.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chubb.FlightService.entity.Seat;
import com.chubb.FlightService.repository.SeatRepository;
import com.chubb.FlightService.service.SeatService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;

    @Override
    public void initializeSeatsForFlight(String flightNumber, LocalDate travelDate, int totalSeats) {
        // Check if seats already exist
        List<Seat> existingSeats = seatRepository.findByFlightNumberAndTravelDate(flightNumber, travelDate);
        if (!existingSeats.isEmpty()) {
            // Already initialized, but verify count matches
            if (existingSeats.size() != totalSeats) {
                throw new IllegalStateException("Seat count mismatch. Expected " + totalSeats + " but found " + existingSeats.size());
            }
            return; // Already initialized
        }

        List<Seat> seats = new ArrayList<>();
        int rows = (totalSeats + 5) / 6; // Assuming 6 seats per row (A, B, C, D, E, F)
        char[] seatLetters = {'A', 'B', 'C', 'D', 'E', 'F'};

        int seatCount = 0;
        for (int row = 1; row <= rows && seatCount < totalSeats; row++) {
            for (char letter : seatLetters) {
                if (seatCount >= totalSeats) break;
                
                String seatNumber = row + String.valueOf(letter);
                Seat seat = Seat.builder()
                        .flightNumber(flightNumber)
                        .travelDate(travelDate)
                        .seatNumber(seatNumber)
                        .status(Seat.SeatStatus.AVAILABLE)
                        .build();
                seats.add(seat);
                seatCount++;
            }
        }

        seatRepository.saveAll(seats);
        seatRepository.flush(); // Force flush to ensure seats are persisted
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableSeats(String flightNumber, LocalDate travelDate) {
        return seatRepository.findByFlightNumberAndTravelDateAndStatus(
                flightNumber, travelDate, Seat.SeatStatus.AVAILABLE
        ).stream()
                .map(Seat::getSeatNumber)
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public boolean bookSeats(String flightNumber, LocalDate travelDate, List<String> seatNumbers, String bookingId) {
        // Ensure seats are initialized first
        List<Seat> existingSeats = seatRepository.findByFlightNumberAndTravelDate(flightNumber, travelDate);
        if (existingSeats.isEmpty()) {
            throw new IllegalStateException("Seats not initialized for flight " + flightNumber + " on " + travelDate + ". Please initialize seats first.");
        }
        
        List<Seat> seatsToBook = new ArrayList<>();
        
        // Lock and check all seats
        for (String seatNumber : seatNumbers) {
            Seat seat = seatRepository.findByFlightNumberAndTravelDateAndSeatNumberForUpdate(
                    flightNumber, travelDate, seatNumber
            ).orElseThrow(() -> new IllegalArgumentException(
                    "Seat " + seatNumber + " not found for flight " + flightNumber + " on " + travelDate + ". Available seats: " + 
                    seatRepository.findByFlightNumberAndTravelDateAndStatus(flightNumber, travelDate, Seat.SeatStatus.AVAILABLE)
                        .stream().map(Seat::getSeatNumber).collect(java.util.stream.Collectors.joining(", "))
            ));

            if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
                throw new IllegalStateException("Seat " + seatNumber + " is already booked (Status: " + seat.getStatus() + ", BookingId: " + seat.getBookingId() + ")");
            }

            seatsToBook.add(seat);
        }

        // Book all seats atomically
        for (Seat seat : seatsToBook) {
            seat.setStatus(Seat.SeatStatus.BOOKED);
            seat.setBookingId(bookingId);
        }

        seatRepository.saveAll(seatsToBook);
        seatRepository.flush(); // Force flush to ensure changes are persisted
        return true;
    }

    @Override
    public void releaseSeats(String bookingId) {
        List<Seat> seats = seatRepository.findByBookingId(bookingId);
        for (Seat seat : seats) {
            seat.setStatus(Seat.SeatStatus.AVAILABLE);
            seat.setBookingId(null);
        }
        seatRepository.saveAll(seats);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean areSeatsAvailable(String flightNumber, LocalDate travelDate, List<String> seatNumbers) {
        for (String seatNumber : seatNumbers) {
            Optional<Seat> seatOpt = seatRepository.findByFlightNumberAndTravelDateAndSeatNumberForUpdate(
                    flightNumber, travelDate, seatNumber
            );
            if (seatOpt.isEmpty() || seatOpt.get().getStatus() != Seat.SeatStatus.AVAILABLE) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seat> getSeatsByBookingId(String bookingId) {
        return seatRepository.findByBookingId(bookingId);
    }
}

