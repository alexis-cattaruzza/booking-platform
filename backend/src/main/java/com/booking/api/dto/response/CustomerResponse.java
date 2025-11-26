package com.booking.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponse {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime lastVisit;
}
