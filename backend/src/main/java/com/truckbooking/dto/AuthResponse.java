package com.truckbooking.dto;

import com.truckbooking.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private Role role;
    private Long userId;
    private Long customerId;
}
