package com.chubb.BookingService.exception;

public class BookingAlreadyCancelledException extends RuntimeException {

    public BookingAlreadyCancelledException(String pnr) {
        super("Booking is already cancelled for PNR: " + pnr);
    }
}