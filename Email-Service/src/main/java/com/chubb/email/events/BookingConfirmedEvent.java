package com.chubb.email.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingConfirmedEvent {

    // Basic identifiers
    private String pnr;
    private String contactEmail;
    private String primaryPassengerName;

    // Flight info
    private String airline;          // e.g. VISTARA
    private String flightNumber;     // e.g. UK-123
    private String tripType;         // ONE_WAY / ROUND_TRIP
    private String source;           // DELHI
    private String destination;      // MUMBAI

    // Times (keep as String to keep it simple)
    private String departureDateTime;  // "2025-12-20 08:30"
    private String arrivalDateTime;    // "2025-12-20 10:30"

    // Passenger / booking details
    private Integer seatsBooked;       // 2
    private String mealType;          // VEG_JAIN
    private String status;            // CONFIRMED
}