package com.booking.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBusinessRequest {

    @Size(max = 255, message = "Business name must not exceed 255 characters")
    private String businessName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 10, message = "Postal code must not exceed 10 characters")
    private String postalCode;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Email(message = "Email must be valid")
    private String email;

    private String category; // HAIRDRESSER, BEAUTY, HEALTH, SPORT, GARAGE, OTHER

    private String logoUrl;
}
