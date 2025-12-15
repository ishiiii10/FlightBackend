package com.chubb.BookingService.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateBookingResponse {

    private String pnr;
    private String message;
}