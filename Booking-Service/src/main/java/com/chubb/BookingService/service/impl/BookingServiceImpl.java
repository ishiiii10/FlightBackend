package com.chubb.BookingService.service.impl;

import com.chubb.BookingService.client.EmailClient;
import com.chubb.BookingService.client.FlightClient;
import com.chubb.BookingService.dto.BookingConfirmedEmailRequest;
import com.chubb.BookingService.dto.BookingSummaryResponse;
import com.chubb.BookingService.dto.CancelBookingResponse;
import com.chubb.BookingService.dto.CreateBookingRequest;
import com.chubb.BookingService.dto.CreateBookingResponse;
import com.chubb.BookingService.entity.Booking;
import com.chubb.BookingService.entity.Passenger;
import com.chubb.BookingService.enums.Booking_Status;
import com.chubb.BookingService.exception.BookingAlreadyCancelledException;
import com.chubb.BookingService.exception.BookingNotFoundException;
import com.chubb.BookingService.repository.BookingRepository;
import com.chubb.BookingService.service.BookingService;
import com.chubb.BookingService.util.PnrGenerator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final FlightClient flightClient;
    private final EmailClient emailClient;

    @Override
    @Transactional
    public CreateBookingResponse createBooking(
            CreateBookingRequest request,
            String userId,
            String email
    ) {

        // Basic duplicate check: same user, flight and date with an active booking
        boolean exists =
                bookingRepository.existsByFlightNumberAndTravelDateAndUserIdAndStatusIn(
                        request.getFlightNumber(),
                        request.getTravelDate(),
                        userId,
                        java.util.List.of(
                                Booking_Status.CONFIRMED,
                                Booking_Status.PENDING
                        )
                );

        if (exists) {
            throw new RuntimeException("Duplicate active booking found for this flight/date");
        }

        String pnr = PnrGenerator.generatePNR();

        Booking booking = new Booking();
        booking.setPnr(pnr);
        booking.setFlightNumber(request.getFlightNumber());
        booking.setTravelDate(request.getTravelDate());
        booking.setTripType(request.getTripType());
        booking.setSeatsBooked(request.getSeatsBooked());
        booking.setContactEmail(email);
        booking.setUserId(userId);          // 🔐 OWNERSHIP
        booking.setStatus(Booking_Status.CONFIRMED);
        booking.setCreatedAt(LocalDateTime.now());

        // Map passengers list
        var passengers = request.getPassengers().stream()
                .map(p -> Passenger.builder()
                        .name(p.getName())
                        .gender(p.getGender())
                        .mealType(p.getMealType())
                        .booking(booking)
                        .build())
                .toList();
        booking.setPassengers(passengers);

        // For backward compatibility, copy primary passenger details to flat columns
        booking.setPassengerName(passengers.get(0).getName());
        booking.setGender(passengers.get(0).getGender());
        booking.setMealType(passengers.get(0).getMealType());

        try {
            // 1) Reserve seats atomically in Flight-Service (pessimistic lock)
            flightClient.reserveSeats(request.getFlightNumber(), request.getSeatsBooked());

            // 2) Persist booking in Booking-Service
            bookingRepository.save(booking);

            // 3) Notify Email-Service (fire-and-forget)
            BookingConfirmedEmailRequest emailRequest = BookingConfirmedEmailRequest.builder()
                    .pnr(pnr)
                    .contactEmail(booking.getContactEmail())
                    .primaryPassengerName(booking.getPassengers().get(0).getName())
                    .flightNumber(booking.getFlightNumber())
                    .tripType(booking.getTripType())
                    .seatsBooked(booking.getSeatsBooked())
                    .mealType(booking.getPassengers().get(0).getMealType())
                    .status(booking.getStatus().name())
                    .build();

            try {
                emailClient.sendBookingConfirmation(emailRequest);
            } catch (Exception ex) {
                // log and continue in real system; do not fail booking for email issues
            }

        } catch (Exception ex) {
            // Best-effort compensation if booking persistence fails after seat reservation
            try {
                flightClient.releaseSeats(request.getFlightNumber(), request.getSeatsBooked());
            } catch (Exception ignored) {
                // log in real system
            }
            throw ex;
        }

        return CreateBookingResponse.builder()
                .pnr(pnr)
                .message("Booking created successfully")
                .build();
    }

    @Override
    public BookingSummaryResponse getBookingByPnr(String pnr, String userId) {

        Booking booking = bookingRepository.findByPnr(pnr)
                .orElseThrow(() -> new BookingNotFoundException(pnr));

        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied to this booking");
        }

        return BookingSummaryResponse.builder()
                .pnr(booking.getPnr())
                .status(booking.getStatus())
                .flightNumber(booking.getFlightNumber())
                .travelDate(booking.getTravelDate())
                .passengerName(booking.getPassengerName())
                .seatsBooked(booking.getSeatsBooked())
                .build();
    }
    
    @Override
    public List<BookingSummaryResponse> getMyBookings(String userId) {

        return bookingRepository.findByUserId(userId)
                .stream()
                .map(booking -> BookingSummaryResponse.builder()
                        .pnr(booking.getPnr())
                        .status(booking.getStatus())
                        .flightNumber(booking.getFlightNumber())
                        .travelDate(booking.getTravelDate())
                        .passengerName(booking.getPassengerName())
                        .seatsBooked(booking.getSeatsBooked())
                        .build()
                )
                .toList();
    }

    @Override
    public CancelBookingResponse cancelBooking(String pnr, String userId) {

        Booking booking = bookingRepository.findByPnr(pnr)
                .orElseThrow(() -> new BookingNotFoundException(pnr));

        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied to cancel this booking");
        }

        if (booking.getStatus() == Booking_Status.CANCELLED) {
            throw new BookingAlreadyCancelledException(pnr);
        }

        booking.setStatus(Booking_Status.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        // Release seats back to Flight-Service
        flightClient.releaseSeats(booking.getFlightNumber(), booking.getSeatsBooked());

        return CancelBookingResponse.builder()
                .pnr(pnr)
                .status("CANCELLED")
                .message("Booking cancelled successfully")
                .build();
    }
}