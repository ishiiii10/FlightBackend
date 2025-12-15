package com.chubb.BookingService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chubb.BookingService.entity.Booking;
import com.chubb.BookingService.enums.Booking_Status;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByPnr(String pnr);

    List<Booking> findByUserId(String userId);

    boolean existsByFlightNumberAndTravelDateAndPassengerNameAndStatusIn(
            String flightNumber,
            LocalDate travelDate,
            String passengerName,
            Iterable<Booking_Status> statuses
    );
}