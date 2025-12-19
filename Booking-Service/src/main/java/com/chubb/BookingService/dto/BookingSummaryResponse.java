package com.chubb.BookingService.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.chubb.BookingService.enums.Booking_Status;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingSummaryResponse {

    private String pnr;
    private Booking_Status status;
    private String flightNumber;
    private String source;
    private String destination;
    private LocalDate travelDate;
    private LocalDateTime bookingDate;
    private String passengerName;
    private Integer seatsBooked;
    private List<String> seatNumbers;
    private BigDecimal totalAmount;
    private String returnFlightNumber;
    private LocalDate returnTravelDate;
}