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

            StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append("Dear ").append(event.getPrimaryPassengerName()).append(",\n\n");
            bodyBuilder.append("Your booking has been confirmed.\n\n");
            bodyBuilder.append("PNR: ").append(event.getPnr()).append("\n");
            bodyBuilder.append("Trip Type: ").append(event.getTripType()).append("\n\n");
            
            bodyBuilder.append("OUTBOUND FLIGHT:\n");
            bodyBuilder.append("Flight: ").append(event.getFlightNumber()).append(" (").append(event.getAirline()).append(")\n");
            bodyBuilder.append("Route: ").append(event.getSource()).append(" -> ").append(event.getDestination()).append("\n");
            bodyBuilder.append("Travel Date: ").append(event.getTravelDate() != null ? event.getTravelDate() : "").append("\n");
            bodyBuilder.append("Departure: ").append(event.getDepartureDateTime()).append("\n");
            bodyBuilder.append("Arrival: ").append(event.getArrivalDateTime()).append("\n");
            bodyBuilder.append("Seats: ").append(event.getSeatNumbers() != null ? String.join(", ", event.getSeatNumbers()) : "").append("\n");
            bodyBuilder.append("Meal: ").append(event.getMealType()).append("\n\n");
            
            if ("ROUND_TRIP".equals(event.getTripType()) && event.getReturnFlightNumber() != null) {
                bodyBuilder.append("RETURN FLIGHT:\n");
                bodyBuilder.append("Flight: ").append(event.getReturnFlightNumber()).append("\n");
                bodyBuilder.append("Travel Date: ").append(event.getReturnTravelDate() != null ? event.getReturnTravelDate() : "").append("\n");
                bodyBuilder.append("Seats: ").append(event.getReturnSeatNumbers() != null ? String.join(", ", event.getReturnSeatNumbers()) : "").append("\n\n");
            }
            
            bodyBuilder.append("Status: ").append(event.getStatus()).append("\n");
            
            // Add total fare if available
            if (event.getTotalFare() != null && !event.getTotalFare().isEmpty()) {
                bodyBuilder.append("Total Fare: $").append(event.getTotalFare()).append("\n");
            }
            
            bodyBuilder.append("\nThank you for flying with us.");

            String body = bodyBuilder.toString();

            message.setText(body);
            mailSender.send(message);
            log.info("Booking confirmation email sent to {}", event.getContactEmail());
        } catch (Exception ex) {
            log.error("Failed to send booking confirmation email to {}", event.getContactEmail(), ex);
            throw ex; // Re-throw to allow retry mechanisms
        }
    }

    public void sendBookingCancellation(java.util.Map<String, Object> eventMap) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            String contactEmail = (String) eventMap.get("contactEmail");
            message.setTo(contactEmail);
            message.setSubject("Flight Booking Cancellation - PNR " + eventMap.get("pnr"));

            StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append("Dear ").append(eventMap.get("primaryPassengerName")).append(",\n\n");
            bodyBuilder.append("Your booking has been cancelled.\n\n");
            bodyBuilder.append("PNR: ").append(eventMap.get("pnr")).append("\n\n");
            
            bodyBuilder.append("CANCELLED FLIGHT DETAILS:\n");
            bodyBuilder.append("Flight: ").append(eventMap.get("flightNumber"));
            if (eventMap.get("airline") != null && !eventMap.get("airline").toString().isEmpty()) {
                bodyBuilder.append(" (").append(eventMap.get("airline")).append(")");
            }
            bodyBuilder.append("\n");
            bodyBuilder.append("Route: ").append(eventMap.get("source")).append(" -> ").append(eventMap.get("destination")).append("\n");
            bodyBuilder.append("Travel Date: ").append(eventMap.get("travelDate")).append("\n");
            bodyBuilder.append("Departure: ").append(eventMap.get("departureDateTime")).append("\n");
            bodyBuilder.append("Arrival: ").append(eventMap.get("arrivalDateTime")).append("\n");
            
            if (eventMap.get("seatNumbers") != null) {
                @SuppressWarnings("unchecked")
                java.util.List<String> seatNumbers = (java.util.List<String>) eventMap.get("seatNumbers");
                bodyBuilder.append("Seats: ").append(String.join(", ", seatNumbers)).append("\n");
            }
            
            if (eventMap.get("returnFlightNumber") != null) {
                bodyBuilder.append("\nRETURN FLIGHT:\n");
                bodyBuilder.append("Flight: ").append(eventMap.get("returnFlightNumber")).append("\n");
                bodyBuilder.append("Travel Date: ").append(eventMap.get("returnTravelDate")).append("\n");
            }
            
            bodyBuilder.append("\nStatus: CANCELLED\n");
            
            if (eventMap.get("totalFare") != null) {
                bodyBuilder.append("Refund Amount: $").append(eventMap.get("totalFare")).append("\n");
            }
            
            bodyBuilder.append("\nWe're sorry to see you go. If you have any questions, please contact our support team.");

            String body = bodyBuilder.toString();
            message.setText(body);
            mailSender.send(message);
            log.info("Booking cancellation email sent to {}", contactEmail);
        } catch (Exception ex) {
            log.error("Failed to send booking cancellation email to {}", eventMap.get("contactEmail"), ex);
            throw ex; // Re-throw to allow retry mechanisms
        }
    }
}


