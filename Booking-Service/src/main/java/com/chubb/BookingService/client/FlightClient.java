package com.chubb.BookingService.client;

import com.chubb.BookingService.dto.FlightDetailsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "FLIGHTSERVICE")
public interface FlightClient {
    @GetMapping("/flights/internal/{flightNumber}")
    FlightDetailsResponse getFlightDetails(@PathVariable String flightNumber);
}