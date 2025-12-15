package com.chubb.BookingService.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chubb.BookingService.dto.BookingSummaryResponse;
import com.chubb.BookingService.dto.CancelBookingResponse;
import com.chubb.BookingService.dto.CreateBookingRequest;
import com.chubb.BookingService.service.BookingService;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<?> createBooking(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role") String role,
            @RequestBody @Valid CreateBookingRequest request
    ) {
        return ResponseEntity
                .status(201)
                .body(bookingService.createBooking(request, userId, email));
    }

    @GetMapping("/{pnr}")
    public ResponseEntity<BookingSummaryResponse> getBooking(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String pnr
    ) {
        return ResponseEntity.ok(
                bookingService.getBookingByPnr(pnr, userId)
        );
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingSummaryResponse>> getMyBookings(
            @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(
                bookingService.getMyBookings(userId)
        );
    }

    @DeleteMapping("/cancel/{pnr}")
    public ResponseEntity<CancelBookingResponse> cancelBooking(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String pnr
    ) {
        return ResponseEntity.ok(
                bookingService.cancelBooking(pnr, userId)
        );
    }
}