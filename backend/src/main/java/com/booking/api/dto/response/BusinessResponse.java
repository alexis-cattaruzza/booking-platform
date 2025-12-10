package com.booking.api.dto.response;

import com.booking.api.model.Business;
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
    private String status;
    private String ownerFirstName;
    private String ownerLastName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static BusinessResponse fromEntity(Business business) {
        String status;
        if (business.getDeletedAt() != null) {
            status = "DELETED";
        } else if (Boolean.FALSE.equals(business.getIsActive())) {
            status = "SUSPENDED";
        } else {
            status = "ACTIVE";
        }

        return BusinessResponse.builder()
                .id(business.getId())
                .businessName(business.getBusinessName())
                .slug(business.getSlug())
                .category(business.getCategory() != null ? business.getCategory().name() : null)
                .description(business.getDescription())
                .address(business.getAddress())
                .city(business.getCity())
                .postalCode(business.getPostalCode())
                .email(business.getEmail())
                .phone(business.getPhone())
                .logoUrl(business.getLogoUrl())
                .settings(business.getSettings())
                .isActive(business.getIsActive())
                .status(status)
                .ownerFirstName(business.getUser() != null ? business.getUser().getFirstName() : null)
                .ownerLastName(business.getUser() != null ? business.getUser().getLastName() : null)
                .createdAt(business.getCreatedAt())
                .updatedAt(business.getUpdatedAt())
                .deletedAt(business.getDeletedAt())
                .build();
    }
}
