package com.chubb.BookingService.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CancelBookingResponse {

    private String pnr;
    private String status;
    private String message;
}