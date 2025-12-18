package com.chubb.FlightService.repository;

import com.chubb.FlightService.entity.Flight;
import com.chubb.FlightService.enums.City;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

	List<Flight> findBySourceAndDestination(City source, City destination);

	Optional<Flight> findByFlightNumber(String flightNumber);

    // Pessimistic lock for atomic seat updates
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Flight f where f.flightNumber = :flightNumber")
    Optional<Flight> findByFlightNumberForUpdate(@Param("flightNumber") String flightNumber);
}