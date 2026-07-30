package com.truckbooking.controller;

import com.truckbooking.dto.ApiResponse;
import com.truckbooking.dto.ContactRequest;
import com.truckbooking.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> sendContact(@Valid @RequestBody ContactRequest request) {
        emailService.sendContactMessage(
                request.getName(),
                request.getEmail(),
                request.getSubject(),
                request.getMessage()
        );
        return ResponseEntity.ok(ApiResponse.ok("Message sent successfully", null));
    }
}
