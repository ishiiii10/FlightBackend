package com.chubb.FlightService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "seats", 
       uniqueConstraints = @UniqueConstraint(
           name = "uk_flight_date_seat", 
           columnNames = {"flightNumber", "travelDate", "seatNumber"}
       ))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String flightNumber;

    @Column(nullable = false)
    private LocalDate travelDate;

    @Column(nullable = false, length = 10)
    private String seatNumber; // e.g., "1A", "1B", "2C"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    @Column(nullable = true)
    private String bookingId; // PNR of the booking that owns this seat

    public enum SeatStatus {
        AVAILABLE, BOOKED
    }
}

