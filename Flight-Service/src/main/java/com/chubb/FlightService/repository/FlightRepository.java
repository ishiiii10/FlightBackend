package com.chubb.FlightService.repository;

import com.chubb.FlightService.entity.Flight;
import com.chubb.FlightService.enums.City;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    // We will add custom methods later if needed
	List<Flight> findBySourceAndDestination(City source, City destination);
	Optional<Flight> findByFlightNumber(String flightNumber);
}