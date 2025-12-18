package com.chubb.FlightService.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chubb.FlightService.entity.Seat;

import jakarta.persistence.LockModeType;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.flightNumber = :flightNumber AND s.travelDate = :travelDate AND s.seatNumber = :seatNumber")
    Optional<Seat> findByFlightNumberAndTravelDateAndSeatNumberForUpdate(
            @Param("flightNumber") String flightNumber,
            @Param("travelDate") LocalDate travelDate,
            @Param("seatNumber") String seatNumber
    );

    List<Seat> findByFlightNumberAndTravelDateAndStatus(
            String flightNumber,
            LocalDate travelDate,
            Seat.SeatStatus status
    );

    List<Seat> findByFlightNumberAndTravelDate(
            String flightNumber,
            LocalDate travelDate
    );

    List<Seat> findByBookingId(String bookingId);
}

