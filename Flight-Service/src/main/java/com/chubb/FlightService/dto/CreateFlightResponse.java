package com.chubb.FlightService.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateFlightResponse {

    private Long id;
    private String message;
}