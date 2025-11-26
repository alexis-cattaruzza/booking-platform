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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
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

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Créer l'utilisateur
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(User.UserRole.BUSINESS)
                .emailVerified(true) // Temporaire pour MVP - TODO: implémenter vérification email
                .build();

        user = userRepository.save(user);
        log.info("User created with ID: {}", user.getId());

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

        return buildAuthResponse(user, business, accessToken, refreshToken);
    }

    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getEmail());

        // Authentifier l'utilisateur
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Charger l'utilisateur
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Charger le business
        Business business = businessRepository.findByUserId(user.getId())
                .orElse(null);

        // Générer les tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("User logged in successfully: {}", user.getEmail());

        return buildAuthResponse(user, business, accessToken, refreshToken);
    }

    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing access token");

        String userEmail = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new RuntimeException("Invalid refresh token");
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
}
