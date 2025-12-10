package com.booking.api.controller;

import com.booking.api.dto.request.LoginRequest;
import com.booking.api.dto.request.RegisterRequest;
import com.booking.api.dto.response.AuthResponse;
import com.booking.api.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register
     * Inscription d'un nouveau business
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        log.info("Register request received for email: {}", request.getEmail());
        AuthResponse response = authService.register(request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/auth/login
     * Connexion d'un utilisateur existant
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        log.info("Login request received for email: {}", request.getEmail());
        AuthResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/refresh
     * Rafraîchir l'access token avec le refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> request) {
        log.info("Refresh token request received");
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException("Refresh token must be provided");
        }

        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/logout
     * SECURITY: Logout and blacklist JWT token
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest httpRequest) {
        log.info("Logout request received");
        authService.logout(authHeader, httpRequest);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * GET /api/auth/verify-email
     * SECURITY: Verify user email with token
     */
    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        log.info("Email verification request received");
        authService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }

    /**
     * POST /api/auth/resend-verification
     * SECURITY: Resend email verification
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(@RequestBody Map<String, String> request) {
        log.info("Resend verification request received");
        String email = request.get("email");

        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email must be provided");
        }

        authService.resendVerificationEmail(email);
        return ResponseEntity.ok(Map.of("message", "Verification email sent"));
    }

    /**
     * POST /api/auth/forgot-password
     * SECURITY: Request password reset
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        log.info("Forgot password request received");
        String email = request.get("email");

        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email must be provided");
        }

        authService.forgotPassword(email, httpRequest);
        return ResponseEntity.ok(Map.of("message", "Password reset email sent"));
    }

    /**
     * POST /api/auth/reset-password
     * SECURITY: Reset password with token
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        log.info("Reset password request received");
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token must be provided");
        }

        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("New password must be provided");
        }

        authService.resetPassword(token, newPassword, httpRequest);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}