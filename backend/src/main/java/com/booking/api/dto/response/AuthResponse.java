package com.booking.api.dto.response;

import com.booking.api.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private UserInfo user;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private UUID id;
        private String email;
        private String firstName;
        private String lastName;
        private String phone;
        private User.UserRole role;
        private boolean emailVerified;
        private BusinessInfo business;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BusinessInfo {
        private UUID id;
        private String businessName;
        private String slug;
        private String category;
    }
}
