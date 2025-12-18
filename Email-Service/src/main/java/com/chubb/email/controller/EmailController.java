package com.chubb.email.controller;

import com.chubb.email.events.BookingConfirmedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
public class EmailController {

    private static final Logger log = LoggerFactory.getLogger(EmailController.class);

    @PostMapping("/booking-confirmed")
    public ResponseEntity<Void> sendBookingConfirmation(@RequestBody BookingConfirmedEvent event) {
        // In a real system, inject a mail service and send a proper email.
        log.info("Sending booking confirmation email: PNR={}, to={}", event.getPnr(), event.getContactEmail());
        return ResponseEntity.accepted().build();
    }
}


