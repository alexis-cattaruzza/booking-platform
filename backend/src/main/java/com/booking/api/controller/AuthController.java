package com.booking.api.controller;

import com.booking.api.dto.request.LoginRequest;
import com.booking.api.dto.request.RegisterRequest;
import com.booking.api.dto.response.AuthResponse;
import com.booking.api.service.AuthService;
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
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request received for email: {}", request.getEmail());
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Registration failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * POST /api/auth/login
     * Connexion d'un utilisateur existant
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Login failed: {}", e.getMessage());
            throw e;
        }
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
            return ResponseEntity.badRequest().build();
        }

        try {
            AuthResponse response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * POST /api/auth/logout
     * Déconnexion (côté client principalement)
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        log.info("Logout request received");
        // Avec JWT, la déconnexion est gérée côté client en supprimant le token
        // On pourrait ajouter une blacklist de tokens ici si nécessaire
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
