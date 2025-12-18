package com.chubb.BookingService.client;

import com.chubb.BookingService.dto.BookingConfirmedEmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "EMAIL-SERVICE")
public interface EmailClient {

    @PostMapping("/emails/booking-confirmed")
    void sendBookingConfirmation(@RequestBody BookingConfirmedEmailRequest request);
}


