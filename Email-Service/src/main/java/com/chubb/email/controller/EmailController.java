package com.chubb.email.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chubb.email.events.BookingConfirmedEvent;
import com.chubb.email.service.EmailService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/booking-confirmed")
    public ResponseEntity<Void> sendBookingConfirmation(@RequestBody BookingConfirmedEvent event) {
        emailService.sendBookingConfirmation(event);
        return ResponseEntity.accepted().build();
    }
}


