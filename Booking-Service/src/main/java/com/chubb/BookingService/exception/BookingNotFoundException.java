package com.chubb.BookingService.exception;

public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(String pnr) {
        super("Booking not found for PNR: " + pnr);
    }
}