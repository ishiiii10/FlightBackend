package com.chubb.BookingService.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "EMAILSERVICE")
public interface EmailClient {

    @PostMapping("/emails/booking-confirmed")
    void sendBookingConfirmation(@RequestBody Map<String, Object> event);

    @PostMapping("/emails/booking-cancelled")
    void sendBookingCancellation(@RequestBody Map<String, Object> event);
}


