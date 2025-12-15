package com.chubb.BookingService.util;

import java.util.UUID;

public class PnrGenerator {

    public static String generatePNR() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}