package com.chubb.BookingService.service.impl;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.chubb.BookingService.client.FlightClient;
import com.chubb.BookingService.dto.BookingSummaryResponse;
import com.chubb.BookingService.dto.CancelBookingResponse;
import com.chubb.BookingService.dto.CreateBookingRequest;
import com.chubb.BookingService.dto.CreateBookingResponse;
import com.chubb.BookingService.entity.Booking;
import com.chubb.BookingService.enums.Booking_Status;
import com.chubb.BookingService.exception.BookingAlreadyCancelledException;
import com.chubb.BookingService.exception.BookingNotFoundException;
import com.chubb.BookingService.repository.BookingRepository;
import com.chubb.BookingService.service.BookingService;
import com.chubb.BookingService.util.PnrGenerator;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final FlightClient flightClient;

    @Override
    public CreateBookingResponse createBooking(
            CreateBookingRequest request,
            String userId,
            String email
    ) {

        boolean exists =
                bookingRepository.existsByFlightNumberAndTravelDateAndPassengerNameAndStatusIn(
                        request.getFlightNumber(),
                        request.getTravelDate(),
                        request.getPassengerName(),
                        java.util.List.of(
                                Booking_Status.CONFIRMED,
                                Booking_Status.PENDING,
                                Booking_Status.CANCELLED
                        )
                );

        if (exists) {
            throw new RuntimeException("Duplicate booking found");
        }

        var flightDetails = flightClient.getFlightDetails(request.getFlightNumber());

        if (flightDetails.getAvailableSeats() < request.getSeatsBooked()) {
            throw new RuntimeException("Insufficient seats available");
        }

        String pnr = PnrGenerator.generatePNR();

        Booking booking = new Booking();
        booking.setPnr(pnr);
        booking.setFlightNumber(request.getFlightNumber());
        booking.setTravelDate(request.getTravelDate());
        booking.setPassengerName(request.getPassengerName());
        booking.setGender(request.getGender());
        booking.setMealType(request.getMealType());
        booking.setTripType(request.getTripType());
        booking.setSeatsBooked(request.getSeatsBooked());
        booking.setContactEmail(email);
        booking.setUserId(userId);          // 🔐 OWNERSHIP
        booking.setStatus(Booking_Status.CONFIRMED);

        bookingRepository.save(booking);

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

        return CancelBookingResponse.builder()
                .pnr(pnr)
                .status("CANCELLED")
                .message("Booking cancelled successfully")
                .build();
    }
}