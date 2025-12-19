package com.chubb.BookingService.dto;

import java.time.LocalDate;
import java.util.List;

import com.chubb.BookingService.enums.Meal_Type;
import com.chubb.BookingService.enums.Trip_Type;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingConfirmedEmailRequest {

    private String pnr;
    private String contactEmail;
    private String primaryPassengerName;

    private String airline;
    private String flightNumber;
    private String source;
    private String destination;

    private LocalDate travelDate;
    private String departureDateTime;
    private String arrivalDateTime;

    private Integer seatsBooked;
    private Meal_Type mealType;
    private Trip_Type tripType;
    private String status;
    private List<String> seatNumbers;
    private String returnFlightNumber;
    private LocalDate returnTravelDate;
    private List<String> returnSeatNumbers;
    private java.math.BigDecimal totalFare;
}


