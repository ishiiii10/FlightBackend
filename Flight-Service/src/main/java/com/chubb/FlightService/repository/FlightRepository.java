package com.chubb.FlightService.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chubb.FlightService.entity.Flight;
import com.chubb.FlightService.enums.City;

import jakarta.persistence.LockModeType;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

	@Query("""
    SELECT f FROM Flight f
    WHERE f.source = :source
      AND f.destination = :destination
      AND DATE(f.departureTime) = :travelDate
""")
List<Flight> findFlightsByRouteAndDate(
    @Param("source") City source,
    @Param("destination") City destination,
    @Param("travelDate") LocalDate travelDate
);

	Optional<Flight> findByFlightNumber(String flightNumber);

    // Pessimistic lock for atomic seat updates
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Flight f where f.flightNumber = :flightNumber")
    Optional<Flight> findByFlightNumberForUpdate(@Param("flightNumber") String flightNumber);
}