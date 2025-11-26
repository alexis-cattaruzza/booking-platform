package com.booking.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusinessResponse {
    private UUID id;
    private String businessName;
    private String slug;
    private String description;
    private String address;
    private String city;
    private String postalCode;
    private String phone;
    private String email;
    private String category;
    private String logoUrl;
    private Map<String, Object> settings;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
