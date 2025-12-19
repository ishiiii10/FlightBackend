package com.chubb.BookingService.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chubb.BookingService.client.EmailClient;
import com.chubb.BookingService.client.FlightClient;
import com.chubb.BookingService.dto.BookingConfirmedEmailRequest;
import com.chubb.BookingService.dto.BookingSummaryResponse;
import com.chubb.BookingService.dto.CancelBookingResponse;
import com.chubb.BookingService.dto.CreateBookingRequest;
import com.chubb.BookingService.dto.CreateBookingResponse;
import com.chubb.BookingService.dto.FlightDetailsResponse;
import com.chubb.BookingService.entity.Booking;
import com.chubb.BookingService.entity.Passenger;
import com.chubb.BookingService.enums.Booking_Status;
import com.chubb.BookingService.enums.Trip_Type;
import com.chubb.BookingService.exception.BookingAlreadyCancelledException;
import com.chubb.BookingService.exception.BookingNotFoundException;
import com.chubb.BookingService.repository.BookingRepository;
import com.chubb.BookingService.service.BookingService;
import com.chubb.BookingService.util.PnrGenerator;

import lombok.RequiredArgsConstructor;

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
        // Validate mandatory fields
        if (request.getFlightNumber() == null || request.getFlightNumber().isBlank()) {
            throw new IllegalArgumentException("Flight number is required");
        }
        
        if (request.getTravelDate() == null) {
            throw new IllegalArgumentException("Travel date is required");
        }
        
        if (request.getTravelDate().isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Travel date cannot be in the past");
        }
        
        if (request.getPassengers() == null || request.getPassengers().isEmpty()) {
            throw new IllegalArgumentException("At least one passenger is required");
        }
        
        if (request.getSeatsBooked() == null || request.getSeatsBooked() <= 0) {
            throw new IllegalArgumentException("Number of seats booked must be greater than 0");
        }
        
        if (request.getContactEmail() == null || request.getContactEmail().isBlank()) {
            throw new IllegalArgumentException("Contact email is required");
        }
        
        // Validate all passengers have required fields
        for (int i = 0; i < request.getPassengers().size(); i++) {
            var passenger = request.getPassengers().get(i);
            if (passenger.getName() == null || passenger.getName().isBlank()) {
                throw new IllegalArgumentException("Passenger " + (i + 1) + ": Name is required");
            }
            if (passenger.getGender() == null) {
                throw new IllegalArgumentException("Passenger " + (i + 1) + ": Gender is required");
            }
            if (passenger.getMealType() == null) {
                throw new IllegalArgumentException("Passenger " + (i + 1) + ": Meal type is required");
            }
            if (passenger.getSeatNumber() == null || passenger.getSeatNumber().isBlank()) {
                throw new IllegalArgumentException("Passenger " + (i + 1) + ": Seat number is required");
            }
        }
        
        // Validate round trip requirements
        if (request.getTripType() == Trip_Type.ROUND_TRIP) {
            if (request.getReturnFlightNumber() == null || request.getReturnFlightNumber().isBlank()) {
                throw new IllegalArgumentException("Return flight number is required for round trip");
            }
            if (request.getReturnTravelDate() == null) {
                throw new IllegalArgumentException("Return travel date is required for round trip");
            }
            if (request.getReturnTravelDate().isBefore(request.getTravelDate())) {
                throw new IllegalArgumentException("Return travel date cannot be before outbound travel date");
            }
            if (request.getReturnPassengers() == null || request.getReturnPassengers().isEmpty()) {
                throw new IllegalArgumentException("Return passengers are required for round trip");
            }
            if (request.getReturnPassengers().size() != request.getPassengers().size()) {
                throw new IllegalArgumentException("Number of return passengers must match outbound passengers");
            }
            
            // Validate all return passengers have required fields
            for (int i = 0; i < request.getReturnPassengers().size(); i++) {
                var passenger = request.getReturnPassengers().get(i);
                if (passenger.getName() == null || passenger.getName().isBlank()) {
                    throw new IllegalArgumentException("Return passenger " + (i + 1) + ": Name is required");
                }
                if (passenger.getGender() == null) {
                    throw new IllegalArgumentException("Return passenger " + (i + 1) + ": Gender is required");
                }
                if (passenger.getMealType() == null) {
                    throw new IllegalArgumentException("Return passenger " + (i + 1) + ": Meal type is required");
                }
                if (passenger.getSeatNumber() == null || passenger.getSeatNumber().isBlank()) {
                    throw new IllegalArgumentException("Return passenger " + (i + 1) + ": Seat number is required");
                }
            }
        }

        // Validate seat numbers match passengers count
        if (request.getPassengers().size() != request.getSeatsBooked()) {
            throw new IllegalArgumentException("Number of passengers must match seats booked");
        }

        // Extract seat numbers from passengers
        List<String> outboundSeatNumbers = request.getPassengers().stream()
                .map(p -> p.getSeatNumber())
                .filter(seat -> seat != null && !seat.isBlank())
                .collect(Collectors.toList());

        // Validate all passengers have seat numbers
        if (outboundSeatNumbers.size() != request.getPassengers().size()) {
            throw new IllegalArgumentException("All passengers must have a seat number assigned");
        }

        // Check for duplicate seat selections
        if (outboundSeatNumbers.size() != outboundSeatNumbers.stream().distinct().count()) {
            throw new IllegalArgumentException("Duplicate seat numbers selected for outbound flight");
        }

        String pnr = PnrGenerator.generatePNR();

        // Get flight details
        var flightDetails = flightClient.getFlightDetails(request.getFlightNumber());

        // Initialize seats if not already done (idempotent)
        try {
            flightClient.initializeSeats(request.getFlightNumber(), request.getTravelDate().toString(), flightDetails.getTotalSeats());
        } catch (Exception e) {
            // If initialization fails, log and check if seats exist
            System.err.println("Seat initialization warning: " + e.getMessage());
            // Continue - seats might already be initialized
        }

        // Validate that selected seats are actually available
        try {
            List<String> availableSeats = flightClient.getAvailableSeats(
                request.getFlightNumber(), 
                request.getTravelDate().toString()
            );
            
            for (String seatNumber : outboundSeatNumbers) {
                if (!availableSeats.contains(seatNumber)) {
                    throw new IllegalArgumentException(
                        "Seat " + seatNumber + " is not available for flight " + request.getFlightNumber() + 
                        " on " + request.getTravelDate() + ". Available seats: " + 
                        (availableSeats.isEmpty() ? "None" : String.join(", ", availableSeats))
                    );
                }
            }
        } catch (IllegalArgumentException e) {
            // Re-throw validation errors
            throw e;
        } catch (Exception e) {
            // For other errors, log but continue - the actual booking will validate again
            System.err.println("Warning: Could not validate seat availability: " + e.getMessage());
        }

        // Book outbound seats atomically
        try {
            com.chubb.BookingService.dto.BookSeatsRequest bookRequest = new com.chubb.BookingService.dto.BookSeatsRequest();
            bookRequest.setTravelDate(request.getTravelDate().toString());
            bookRequest.setSeatNumbers(outboundSeatNumbers);
            bookRequest.setBookingId(pnr);
            flightClient.bookSeats(request.getFlightNumber(), bookRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to book seats: " + e.getMessage(), e);
        }

        // Book return seats if round trip
        List<String> returnSeatNumbers = null;
        if (request.getTripType() == Trip_Type.ROUND_TRIP) {
            returnSeatNumbers = request.getReturnPassengers().stream()
                    .map(p -> p.getSeatNumber())
                    .filter(seat -> seat != null && !seat.isBlank())
                    .collect(Collectors.toList());

            // Validate all return passengers have seat numbers
            if (returnSeatNumbers.size() != request.getReturnPassengers().size()) {
                // Rollback outbound seats
                try {
                    flightClient.releaseSeatsByBookingId(pnr);
                } catch (Exception ignored) {}
                throw new IllegalArgumentException("All return passengers must have a seat number assigned");
            }

            // Check for duplicate return seat selections
            if (returnSeatNumbers.size() != returnSeatNumbers.stream().distinct().count()) {
                // Rollback outbound seats
                try {
                    flightClient.releaseSeatsByBookingId(pnr);
                } catch (Exception ignored) {}
                throw new IllegalArgumentException("Duplicate seat numbers selected for return flight");
            }

            // Initialize return flight seats
            try {
                var returnFlightDetails = flightClient.getFlightDetails(request.getReturnFlightNumber());
                flightClient.initializeSeats(request.getReturnFlightNumber(), request.getReturnTravelDate().toString(), returnFlightDetails.getTotalSeats());
            } catch (Exception e) {
                // Seats may already be initialized
            }

            // Validate that selected return seats are actually available
            try {
                List<String> returnAvailableSeats = flightClient.getAvailableSeats(
                    request.getReturnFlightNumber(), 
                    request.getReturnTravelDate().toString()
                );
                
                for (String seatNumber : returnSeatNumbers) {
                    if (!returnAvailableSeats.contains(seatNumber)) {
                        // Rollback outbound seats
                        try {
                            flightClient.releaseSeatsByBookingId(pnr);
                        } catch (Exception ignored) {}
                        throw new IllegalArgumentException(
                            "Return seat " + seatNumber + " is not available for flight " + request.getReturnFlightNumber() + 
                            " on " + request.getReturnTravelDate() + ". Available seats: " + 
                            (returnAvailableSeats.isEmpty() ? "None" : String.join(", ", returnAvailableSeats))
                        );
                    }
                }
            } catch (IllegalArgumentException e) {
                // Rollback outbound seats
                try {
                    flightClient.releaseSeatsByBookingId(pnr);
                } catch (Exception ignored) {}
                throw e;
            } catch (Exception e) {
                // For other errors, log but continue - the actual booking will validate again
                System.err.println("Warning: Could not validate return seat availability: " + e.getMessage());
            }

            // Book return seats
            try {
                com.chubb.BookingService.dto.BookSeatsRequest returnBookRequest = new com.chubb.BookingService.dto.BookSeatsRequest();
                returnBookRequest.setTravelDate(request.getReturnTravelDate().toString());
                returnBookRequest.setSeatNumbers(returnSeatNumbers);
                returnBookRequest.setBookingId(pnr);
                flightClient.bookSeats(request.getReturnFlightNumber(), returnBookRequest);
            } catch (Exception e) {
                // Rollback outbound seats
                try {
                    flightClient.releaseSeatsByBookingId(pnr);
                } catch (Exception ignored) {}
                throw new RuntimeException("Failed to book return flight seats: " + e.getMessage(), e);
            }
        }

        // Create booking entity
        Booking booking = new Booking();
        booking.setPnr(pnr);
        booking.setFlightNumber(request.getFlightNumber());
        booking.setTravelDate(request.getTravelDate());
        booking.setTripType(request.getTripType());
        booking.setSeatsBooked(request.getSeatsBooked());
        booking.setContactEmail(request.getContactEmail());
        booking.setUserId(userId);
        booking.setStatus(Booking_Status.CONFIRMED);
        booking.setCreatedAt(LocalDateTime.now());

        // Set return flight info if round trip
        if (request.getTripType() == Trip_Type.ROUND_TRIP) {
            booking.setReturnFlightNumber(request.getReturnFlightNumber());
            booking.setReturnTravelDate(request.getReturnTravelDate());
        }

        // Create passengers with seat assignments
        List<Passenger> passengers = new ArrayList<>();
        for (var passengerReq : request.getPassengers()) {
            Passenger passenger = Passenger.builder()
                    .name(passengerReq.getName())
                    .gender(passengerReq.getGender())
                    .mealType(passengerReq.getMealType())
                    .seatNumber(passengerReq.getSeatNumber())
                    .booking(booking)
                    .build();
            passengers.add(passenger);
        }
        booking.setPassengers(passengers);

        // Set primary passenger for backward compatibility
        booking.setPassengerName(passengers.get(0).getName());
        booking.setGender(passengers.get(0).getGender());
        booking.setMealType(passengers.get(0).getMealType());

        // Save booking
        bookingRepository.save(booking);

        // Send email notifications (fire-and-forget)
        try {
            var flightDetailsForEmail = flightClient.getFlightDetails(request.getFlightNumber());
            
            // Build email request matching BookingConfirmedEvent structure
            BookingConfirmedEmailRequest emailRequest = BookingConfirmedEmailRequest.builder()
                    .pnr(pnr)
                    .contactEmail(booking.getContactEmail())
                    .primaryPassengerName(booking.getPassengers().get(0).getName())
                    .flightNumber(booking.getFlightNumber())
                    .airline(flightDetailsForEmail.getAirline() != null ? flightDetailsForEmail.getAirline() : "")
                    .source(flightDetailsForEmail.getSource() != null ? flightDetailsForEmail.getSource() : "")
                    .destination(flightDetailsForEmail.getDestination() != null ? flightDetailsForEmail.getDestination() : "")
                    .departureDateTime(flightDetailsForEmail.getDepartureTime() != null ? flightDetailsForEmail.getDepartureTime().toString() : "")
                    .arrivalDateTime(flightDetailsForEmail.getArrivalTime() != null ? flightDetailsForEmail.getArrivalTime().toString() : "")
                    .travelDate(booking.getTravelDate())
                    .tripType(booking.getTripType())
                    .seatsBooked(booking.getSeatsBooked())
                    .mealType(booking.getPassengers().get(0).getMealType())
                    .status(booking.getStatus().name())
                    .seatNumbers(outboundSeatNumbers)
                    .returnFlightNumber(request.getReturnFlightNumber())
                    .returnTravelDate(request.getReturnTravelDate())
                    .returnSeatNumbers(returnSeatNumbers)
                    .build();

            // Send to booking owner - convert to event structure for Email-Service
            sendEmailNotification(emailRequest);

            // Send to admin (get admin email from config or user service)
            emailRequest.setContactEmail("admin@flightbooking.com"); // TODO: Get from config or user service
            sendEmailNotification(emailRequest);
        } catch (Exception ex) {
            // Log but don't fail booking
            System.err.println("Failed to send email notification: " + ex.getMessage());
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
                .map(booking -> {
                    try {
                        // Fetch flight details to get source, destination, and price
                        FlightDetailsResponse flightDetails = flightClient.getFlightDetails(booking.getFlightNumber());
                        
                        // Get seat numbers from passengers
                        List<String> seatNumbers = booking.getPassengers() != null ?
                                booking.getPassengers().stream()
                                        .map(Passenger::getSeatNumber)
                                        .collect(Collectors.toList()) :
                                new ArrayList<>();
                        
                        // Calculate total amount (price per seat * number of seats)
                        BigDecimal totalAmount = flightDetails.getPrice() != null ?
                                flightDetails.getPrice().multiply(BigDecimal.valueOf(booking.getSeatsBooked())) :
                                BigDecimal.ZERO;
                        
                        // Handle return flight details if round trip
                        String returnFlightNumber = booking.getReturnFlightNumber();
                        LocalDate returnTravelDate = booking.getReturnTravelDate();
                        
                        // If round trip, add return flight price to total
                        if (returnFlightNumber != null && returnTravelDate != null) {
                            try {
                                FlightDetailsResponse returnFlightDetails = flightClient.getFlightDetails(returnFlightNumber);
                                if (returnFlightDetails.getPrice() != null) {
                                    totalAmount = totalAmount.add(
                                            returnFlightDetails.getPrice().multiply(BigDecimal.valueOf(booking.getSeatsBooked()))
                                    );
                                }
                            } catch (Exception e) {
                                // Log but don't fail if return flight details can't be fetched
                                System.err.println("Could not fetch return flight details: " + e.getMessage());
                            }
                        }
                        
                        return BookingSummaryResponse.builder()
                                .pnr(booking.getPnr())
                                .status(booking.getStatus())
                                .flightNumber(booking.getFlightNumber())
                                .source(flightDetails.getSource())
                                .destination(flightDetails.getDestination())
                                .travelDate(booking.getTravelDate())
                                .bookingDate(booking.getCreatedAt() != null ? booking.getCreatedAt() : LocalDateTime.now())
                                .passengerName(booking.getPassengerName())
                                .seatsBooked(booking.getSeatsBooked())
                                .seatNumbers(seatNumbers)
                                .totalAmount(totalAmount)
                                .returnFlightNumber(returnFlightNumber)
                                .returnTravelDate(returnTravelDate)
                                .build();
                    } catch (Exception e) {
                        // If flight details can't be fetched, return basic info
                        System.err.println("Error fetching flight details for booking " + booking.getPnr() + ": " + e.getMessage());
                        List<String> seatNumbers = booking.getPassengers() != null ?
                                booking.getPassengers().stream()
                                        .map(Passenger::getSeatNumber)
                                        .collect(Collectors.toList()) :
                                new ArrayList<>();
                        
                        return BookingSummaryResponse.builder()
                                .pnr(booking.getPnr())
                                .status(booking.getStatus())
                                .flightNumber(booking.getFlightNumber())
                                .source("N/A")
                                .destination("N/A")
                                .travelDate(booking.getTravelDate())
                                .bookingDate(booking.getCreatedAt() != null ? booking.getCreatedAt() : LocalDateTime.now())
                                .passengerName(booking.getPassengerName())
                                .seatsBooked(booking.getSeatsBooked())
                                .seatNumbers(seatNumbers)
                                .totalAmount(BigDecimal.ZERO)
                                .returnFlightNumber(booking.getReturnFlightNumber())
                                .returnTravelDate(booking.getReturnTravelDate())
                                .build();
                    }
                })
                .toList();
    }

    @Override
    @Transactional
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

        // Release seats back to Flight-Service (handles both outbound and return)
        flightClient.releaseSeatsByBookingId(pnr);

        return CancelBookingResponse.builder()
                .pnr(pnr)
                .status("CANCELLED")
                .message("Booking cancelled successfully")
                .build();
    }

    /**
     * Helper method to send email notification by converting BookingConfirmedEmailRequest
     * to the structure expected by Email-Service (BookingConfirmedEvent)
     * Uses a Map that matches BookingConfirmedEvent JSON structure
     */
    private void sendEmailNotification(BookingConfirmedEmailRequest request) {
        // Create a map that matches BookingConfirmedEvent JSON structure
        // Feign will serialize this Map to JSON, and Email-Service will deserialize to BookingConfirmedEvent
        java.util.Map<String, Object> eventMap = new java.util.HashMap<>();
        eventMap.put("pnr", request.getPnr());
        eventMap.put("contactEmail", request.getContactEmail());
        eventMap.put("primaryPassengerName", request.getPrimaryPassengerName());
        eventMap.put("airline", request.getAirline() != null ? request.getAirline() : "");
        eventMap.put("flightNumber", request.getFlightNumber());
        eventMap.put("tripType", request.getTripType() != null ? request.getTripType().name() : "");
        eventMap.put("source", request.getSource() != null ? request.getSource() : "");
        eventMap.put("destination", request.getDestination() != null ? request.getDestination() : "");
        eventMap.put("departureDateTime", request.getDepartureDateTime() != null ? request.getDepartureDateTime() : "");
        eventMap.put("arrivalDateTime", request.getArrivalDateTime() != null ? request.getArrivalDateTime() : "");
        eventMap.put("travelDate", request.getTravelDate() != null ? request.getTravelDate().toString() : "");
        eventMap.put("seatsBooked", request.getSeatsBooked());
        eventMap.put("mealType", request.getMealType() != null ? request.getMealType().name() : "");
        eventMap.put("status", request.getStatus());
        eventMap.put("seatNumbers", request.getSeatNumbers());
        eventMap.put("returnFlightNumber", request.getReturnFlightNumber());
        eventMap.put("returnTravelDate", request.getReturnTravelDate() != null ? request.getReturnTravelDate().toString() : null);
        eventMap.put("returnSeatNumbers", request.getReturnSeatNumbers());
        
        // Send using Feign client
        emailClient.sendBookingConfirmation(eventMap);
    }
}
