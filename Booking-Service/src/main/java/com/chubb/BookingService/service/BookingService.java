package com.chubb.BookingService.service;

import java.util.List;

import com.chubb.BookingService.dto.BookingSummaryResponse;
import com.chubb.BookingService.dto.CancelBookingResponse;
import com.chubb.BookingService.dto.CreateBookingRequest;
import com.chubb.BookingService.dto.CreateBookingResponse;

public interface BookingService {

    CreateBookingResponse createBooking(
            CreateBookingRequest request,
            String userId,
            String email
    );

    BookingSummaryResponse getBookingByPnr(String pnr, String userId);

    CancelBookingResponse cancelBooking(String pnr, String userId);
    
    List<BookingSummaryResponse> getMyBookings(String userId);
}