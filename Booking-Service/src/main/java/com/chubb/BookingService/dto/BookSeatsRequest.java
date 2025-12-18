package com.chubb.BookingService.dto;

import lombok.Data;
import java.util.List;

@Data
public class BookSeatsRequest {
    private String travelDate;
    private List<String> seatNumbers;
    private String bookingId;
}

