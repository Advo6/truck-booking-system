package com.truckbooking.controller;

import com.truckbooking.dto.ApiResponse;
import com.truckbooking.dto.TruckRequest;
import com.truckbooking.dto.TruckResponse;
import com.truckbooking.service.TruckService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trucks")
@RequiredArgsConstructor
public class TruckController {

    private final TruckService truckService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TruckResponse>>> getAllTrucks() {
        return ResponseEntity.ok(ApiResponse.ok(truckService.getAllTrucks()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TruckResponse>> getTruck(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(truckService.getTruckById(id)));
    }
}
