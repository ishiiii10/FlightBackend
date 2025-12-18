package com.chubb.email.service;

import com.chubb.email.events.BookingConfirmedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public void sendBookingConfirmation(BookingConfirmedEvent event) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(event.getContactEmail());
            message.setSubject("Flight Ticket Confirmation - PNR " + event.getPnr());

            String body = """
                    Dear %s,
                    
                    Your booking has been confirmed.
                    
                    PNR: %s
                    Flight: %s (%s)
                    Route: %s -> %s
                    Departure: %s
                    Arrival: %s
                    Seats: %d
                    Meal: %s
                    Status: %s
                    
                    Thank you for flying with us.
                    """.formatted(
                    event.getPrimaryPassengerName(),
                    event.getPnr(),
                    event.getFlightNumber(),
                    event.getAirline(),
                    event.getSource(),
                    event.getDestination(),
                    event.getDepartureDateTime(),
                    event.getArrivalDateTime(),
                    event.getSeatsBooked(),
                    event.getMealType(),
                    event.getStatus()
            );

            message.setText(body);
            mailSender.send(message);
            log.info("Booking confirmation email sent to {}", event.getContactEmail());
        } catch (Exception ex) {
            log.error("Failed to send booking confirmation email to {}", event.getContactEmail(), ex);
        }
    }
}


