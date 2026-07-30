package com.truckbooking.service;

import com.truckbooking.model.Booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.business.name}")
    private String businessName;

    @Value("${app.business.email}")
    private String businessEmail;

    /** Sends booking confirmation to the customer and a new-booking alert to Noko Transport. */
    public void sendBookingConfirmation(Booking booking) {
        if (!mailEnabled) {
            log.info("Email disabled — would send booking {} to customer {} and alert to {}",
                    booking.getBookingReference(),
                    booking.getCustomer().getEmail(),
                    businessEmail);
            return;
        }

        sendCustomerConfirmation(booking);
        sendBusinessBookingAlert(booking);
    }

    private void sendCustomerConfirmation(Booking booking) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(booking.getCustomer().getEmail());
            message.setSubject("Booking Confirmation - " + booking.getBookingReference());
            message.setText(buildConfirmationBody(booking));
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send customer confirmation for {}: {}",
                    booking.getBookingReference(), e.getMessage());
        }
    }

    private void sendBusinessBookingAlert(Booking booking) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(businessEmail);
            message.setReplyTo(booking.getCustomer().getEmail());
            message.setSubject("New Booking Request - " + booking.getBookingReference());
            message.setText(buildBusinessAlertBody(booking));
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send business alert for {}: {}",
                    booking.getBookingReference(), e.getMessage());
        }
    }

    public void sendContactMessage(String name, String email, String subject, String body) {
        if (!mailEnabled) {
            log.info("Email disabled — contact from {} <{}>: {}", name, email, subject);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(fromEmail);
            message.setReplyTo(email);
            message.setSubject("Contact: " + subject);
            message.setText("From: " + name + " (" + email + ")\n\n" + body);
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send contact email: {}", e.getMessage());
        }
    }

    private String buildConfirmationBody(Booking booking) {
        return String.format("""
                Dear %s,

                Thank you for booking with %s!

                Booking Reference: %s
                Truck: %s (%s)
                Pickup Date: %s
                Return Date: %s
                Pickup Address: %s
                Delivery Address: %s
                Status: %s

                We will review your booking and confirm shortly.

                Best regards,
                %s Team
                """,
                booking.getCustomer().getName(),
                businessName,
                booking.getBookingReference(),
                booking.getTruck().getTruckNumber(),
                booking.getTruck().getRegistration(),
                booking.getPickupDate(),
                booking.getReturnDate(),
                booking.getPickupAddress(),
                booking.getDeliveryAddress(),
                booking.getStatus(),
                businessName
        );
    }

    private String buildBusinessAlertBody(Booking booking) {
        String cargo = booking.getCargoDescription() != null && !booking.getCargoDescription().isBlank()
                ? booking.getCargoDescription() : "Not provided";
        String weight = booking.getWeight() != null
                ? booking.getWeight() + " kg" : "Not provided";
        String instructions = booking.getSpecialInstructions() != null && !booking.getSpecialInstructions().isBlank()
                ? booking.getSpecialInstructions() : "None";

        return String.format("""
                New booking received — action required

                Booking Reference: %s
                Status: %s

                CUSTOMER
                Name: %s
                Phone: %s
                Email: %s

                TRUCK
                %s (%s)

                DATES
                Pickup: %s
                Return: %s

                LOCATIONS
                Pickup Address: %s
                Delivery Address: %s

                CARGO
                Description: %s
                Weight: %s
                Special Instructions: %s

                Log in to the admin dashboard to approve or reject this booking.

                — %s Booking System
                """,
                booking.getBookingReference(),
                booking.getStatus(),
                booking.getCustomer().getName(),
                booking.getCustomer().getPhone(),
                booking.getCustomer().getEmail(),
                booking.getTruck().getTruckNumber(),
                booking.getTruck().getRegistration(),
                booking.getPickupDate(),
                booking.getReturnDate(),
                booking.getPickupAddress(),
                booking.getDeliveryAddress(),
                cargo,
                weight,
                instructions,
                businessName
        );
    }
}
