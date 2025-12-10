package com.booking.api.service;

import com.booking.api.dto.request.LoginRequest;
import com.booking.api.dto.request.RegisterRequest;
import com.booking.api.dto.response.AuthResponse;
import com.booking.api.model.Business;
import com.booking.api.model.Subscription;
import com.booking.api.model.User;
import com.booking.api.repository.BusinessRepository;
import com.booking.api.repository.SubscriptionRepository;
import com.booking.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final EmailService emailService;
    private final AuditService auditService;
    private final TokenBlacklistService tokenBlacklistService;

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    // SECURITY: Account lockout configuration
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;

    @Transactional
    public AuthResponse register(RegisterRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // SECURITY: Generate email verification token
        String verificationToken = java.util.UUID.randomUUID().toString();
        java.time.LocalDateTime tokenExpiration = java.time.LocalDateTime.now().plusHours(24);

        // Créer l'utilisateur
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(User.UserRole.BUSINESS)
                .emailVerified(false) // SECURITY: Require email verification
                .emailVerificationToken(verificationToken)
                .emailVerificationTokenExpiresAt(tokenExpiration)
                .build();

        user = userRepository.save(user);
        log.info("User created with ID: {}", user.getId());

        // SECURITY: Send verification email
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), verificationToken);
        log.info("Verification email sent to: {}", user.getEmail());

        // Générer un slug unique pour le business
        String baseSlug = generateSlug(request.getBusinessName());
        String uniqueSlug = ensureUniqueSlug(baseSlug);

        // Créer le business associé
        Business business = Business.builder()
                .user(user)
                .businessName(request.getBusinessName())
                .slug(uniqueSlug)
                .email(request.getEmail())
                .phone(request.getPhone())
                .isActive(true)
                .build();

        business = businessRepository.save(business);
        log.info("Business created with ID: {} and slug: {}", business.getId(), business.getSlug());

        // Créer la subscription FREE par défaut
        Subscription subscription = Subscription.builder()
                .business(business)
                .plan(Subscription.SubscriptionPlan.FREE)
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .build();

        subscriptionRepository.save(subscription);
        log.info("Free subscription created for business: {}", business.getId());

        // Générer les tokens JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // SECURITY: Log registration audit
        auditService.logAudit(
            user,
            com.booking.api.model.AuditLog.Actions.REGISTER,
            com.booking.api.model.AuditLog.AuditStatus.SUCCESS,
            httpRequest,
            "Business",
            business.getId().toString(),
            null,
            auditService.createDetails("businessName", business.getBusinessName())
        );

        return buildAuthResponse(user, business, accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        log.info("User login attempt: {}", request.getEmail());

        // SECURITY: Load user first to check account lockout
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // SECURITY: Check if account is locked
        if (user.getAccountLockedUntil() != null &&
            user.getAccountLockedUntil().isAfter(java.time.LocalDateTime.now())) {
            long minutesRemaining = java.time.Duration.between(
                java.time.LocalDateTime.now(),
                user.getAccountLockedUntil()
            ).toMinutes();
            throw new SecurityException(
                String.format("Account locked due to multiple failed login attempts. Try again in %d minutes.",
                    minutesRemaining)
            );
        }

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // SECURITY: Reset failed attempts on successful login
            if (user.getFailedLoginAttempts() > 0) {
                user.setFailedLoginAttempts(0);
                user.setAccountLockedUntil(null);
                userRepository.save(user);
                log.info("Failed login attempts reset for user: {}", user.getEmail());
            }

        } catch (BadCredentialsException ex) {
            // SECURITY: Increment failed attempts
            handleFailedLoginAttempt(user);

            // SECURITY: Log failed login attempt
            auditService.logAudit(
                user,
                com.booking.api.model.AuditLog.Actions.LOGIN_FAILED,
                com.booking.api.model.AuditLog.AuditStatus.FAILURE,
                httpRequest,
                "Authentication failed - invalid credentials"
            );

            throw new BadCredentialsException("Invalid email or password"); // 401
        }

        // Charger le business
        Business business = businessRepository.findByUserId(user.getId())
                .orElse(null);

        // Générer les tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("User logged in successfully: {}", user.getEmail());

        // SECURITY: Log successful login
        auditService.logAudit(
            user,
            com.booking.api.model.AuditLog.Actions.LOGIN,
            com.booking.api.model.AuditLog.AuditStatus.SUCCESS,
            httpRequest
        );

        return buildAuthResponse(user, business, accessToken, refreshToken);
    }

    /**
     * SECURITY: Handle failed login attempt and implement account lockout
     */
    private void handleFailedLoginAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setAccountLockedUntil(java.time.LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
            log.warn("Account locked for user {} after {} failed attempts", user.getEmail(), attempts);
        } else {
            log.warn("Failed login attempt {} of {} for user {}", attempts, MAX_FAILED_ATTEMPTS, user.getEmail());
        }

        userRepository.save(user);
    }

    /**
     * SECURITY: Verify user email with token
     */
    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new com.booking.api.exception.NotFoundException("Invalid verification token"));

        // Check if token has expired
        if (user.getEmailVerificationTokenExpiresAt() == null ||
            user.getEmailVerificationTokenExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new com.booking.api.exception.BadRequestException(
                "Verification token has expired. Please request a new verification email.");
        }

        // Check if already verified
        if (user.getEmailVerified()) {
            throw new com.booking.api.exception.BadRequestException("Email already verified");
        }

        // Verify email
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiresAt(null);
        userRepository.save(user);

        log.info("Email verified successfully for user: {}", user.getEmail());
    }

    /**
     * SECURITY: Resend verification email
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new com.booking.api.exception.NotFoundException("User not found"));

        // Check if already verified
        if (user.getEmailVerified()) {
            throw new com.booking.api.exception.BadRequestException("Email already verified");
        }

        // Generate new token
        String verificationToken = java.util.UUID.randomUUID().toString();
        java.time.LocalDateTime tokenExpiration = java.time.LocalDateTime.now().plusHours(24);

        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiresAt(tokenExpiration);
        userRepository.save(user);

        // Send email
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), verificationToken);
        log.info("Verification email resent to: {}", user.getEmail());
    }

    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing access token");

        String userEmail = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new SecurityException("Invalid refresh token");
        }

        String newAccessToken = jwtService.generateToken(userDetails);
        Business business = businessRepository.findByUserId(user.getId()).orElse(null);

        return buildAuthResponse(user, business, newAccessToken, refreshToken);
    }

    private AuthResponse buildAuthResponse(User user, Business business, String accessToken, String refreshToken) {
        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole())
                .emailVerified(user.getEmailVerified())
                .build();

        if (business != null) {
            AuthResponse.BusinessInfo businessInfo = AuthResponse.BusinessInfo.builder()
                    .id(business.getId())
                    .businessName(business.getBusinessName())
                    .slug(business.getSlug())
                    .category(business.getCategory() != null ? business.getCategory().name() : null)
                    .build();
            userInfo.setBusiness(businessInfo);
        }

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userInfo)
                .build();
    }

    /**
     * Génère un slug SEO-friendly à partir du nom du business
     * Exemple: "Coiffeur Marie" -> "coiffeur-marie"
     */
    private String generateSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

    /**
     * S'assure que le slug est unique en ajoutant un suffixe numérique si nécessaire
     * Exemple: si "coiffeur-marie" existe déjà -> "coiffeur-marie-1"
     */
    private String ensureUniqueSlug(String baseSlug) {
        String slug = baseSlug;
        int counter = 1;

        while (businessRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    /**
     * SECURITY: Request password reset
     */
    @Transactional
    public void forgotPassword(String email, jakarta.servlet.http.HttpServletRequest httpRequest) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new com.booking.api.exception.NotFoundException("User not found"));

        // SECURITY: Generate password reset token (1 hour validity)
        String resetToken = java.util.UUID.randomUUID().toString();
        java.time.LocalDateTime tokenExpiration = java.time.LocalDateTime.now().plusHours(1);

        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiresAt(tokenExpiration);
        userRepository.save(user);

        // Send password reset email
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetToken);
        log.info("Password reset email sent to: {}", user.getEmail());

        // SECURITY: Log password reset request
        auditService.logAudit(
            user,
            com.booking.api.model.AuditLog.Actions.PASSWORD_RESET_REQUESTED,
            com.booking.api.model.AuditLog.AuditStatus.SUCCESS,
            httpRequest
        );
    }

    /**
     * SECURITY: Reset password with token
     */
    @Transactional
    public void resetPassword(String token, String newPassword, jakarta.servlet.http.HttpServletRequest httpRequest) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new com.booking.api.exception.NotFoundException("Invalid reset token"));

        // SECURITY: Check if token has expired
        if (user.getPasswordResetTokenExpiresAt() == null ||
            user.getPasswordResetTokenExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new com.booking.api.exception.BadRequestException(
                "Reset token has expired. Please request a new password reset.");
        }

        // SECURITY: Update password and invalidate token
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);

        // SECURITY: Reset failed login attempts on password reset
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);

        userRepository.save(user);

        log.info("Password reset successfully for user: {}", user.getEmail());

        // SECURITY: Log password reset completion
        auditService.logAudit(
            user,
            com.booking.api.model.AuditLog.Actions.PASSWORD_RESET_COMPLETED,
            com.booking.api.model.AuditLog.AuditStatus.SUCCESS,
            httpRequest
        );
    }

    /**
     * SECURITY: Logout user and blacklist JWT token
     * @param authHeader Authorization header containing Bearer token
     * @param httpRequest HTTP request for audit logging
     */
    @Transactional
    public void logout(String authHeader, jakarta.servlet.http.HttpServletRequest httpRequest) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String userEmail = jwtService.extractUsername(token);

        // Get user for audit logging
        User user = userRepository.findByEmail(userEmail).orElse(null);

        // SECURITY: Calculate token expiration and blacklist it
        long expirationSeconds = jwtService.getExpirationTimeInSeconds(token);
        tokenBlacklistService.blacklistToken(token, expirationSeconds);

        log.info("SECURITY: User logged out: {}", userEmail);

        // SECURITY: Log logout action
        auditService.logAudit(
            user,
            com.booking.api.model.AuditLog.Actions.LOGOUT,
            com.booking.api.model.AuditLog.AuditStatus.SUCCESS,
            httpRequest
        );
    }
}
