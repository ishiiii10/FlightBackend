package com.chubb.BookingService.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chubb.BookingService.entity.Booking;
import com.chubb.BookingService.enums.Booking_Status;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByPnr(String pnr);

    List<Booking> findByUserId(String userId);

    boolean existsByFlightNumberAndTravelDateAndUserIdAndStatusIn(
            String flightNumber,
            LocalDate travelDate,
            String userId,
            Iterable<Booking_Status> statuses
    );

    List<Booking> findByFlightNumberAndTravelDateAndStatus(
            String flightNumber,
            LocalDate travelDate,
            Booking_Status status
    );
}