package com.chubb.FlightService.service;

import com.chubb.FlightService.dto.CreateFlightRequest;
import com.chubb.FlightService.dto.CreateFlightResponse;
import com.chubb.FlightService.dto.FlightSummaryResponse;
import com.chubb.FlightService.dto.InternalFlightResponse;
import com.chubb.FlightService.enums.City;

import java.util.List;

public interface FlightService {

    CreateFlightResponse createFlight(CreateFlightRequest request, String role);

    List<FlightSummaryResponse> getAllFlights();

    FlightSummaryResponse getFlightById(Long id);

    List<FlightSummaryResponse> searchFlights(City source, City destination);

    InternalFlightResponse getInternalFlightDetails(String flightNumber);
}