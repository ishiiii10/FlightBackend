package com.chubb.BookingService.dto;

import com.chubb.BookingService.enums.Booking_Status;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class BookingSummaryResponse {

    private String pnr;
    private Booking_Status status;
    private String flightNumber;
    private LocalDate travelDate;
    private String passengerName;
    private Integer seatsBooked;
}