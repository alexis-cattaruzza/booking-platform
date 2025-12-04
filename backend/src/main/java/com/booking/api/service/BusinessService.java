package com.booking.api.service;

import com.booking.api.dto.request.UpdateBusinessRequest;
import com.booking.api.dto.response.BusinessResponse;
import com.booking.api.dto.response.ServiceResponse;
import com.booking.api.exception.BadRequestException;
import com.booking.api.exception.NotFoundException;
import com.booking.api.model.Business;
import com.booking.api.model.User;
import com.booking.api.repository.BusinessRepository;
import com.booking.api.repository.ServiceRepository;
import com.booking.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;

    /**
     * Récupère le business de l'utilisateur connecté
     */
    public BusinessResponse getMyBusiness() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Getting business for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Business business = businessRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException("Business not found for user"));

        return mapToResponse(business);
    }

    /**
     * Récupère un business public par son slug
     */
    public BusinessResponse getBusinessBySlug(String slug) {
        log.info("Getting public business with slug: {}", slug);

        Business business = businessRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Business not found with slug: " + slug));

        if (!business.getIsActive()) {
            throw new BadRequestException("Business is not active");
        }

        return mapToResponse(business);
    }

    /**
     * Met à jour le business de l'utilisateur connecté
     */
    @Transactional
    public BusinessResponse updateMyBusiness(UpdateBusinessRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Updating business for user: {}", email);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found"));

        Business business = businessRepository.findByUserId(user.getId())
            .orElseThrow(() -> new NotFoundException("Business not found"));
        
        if (request.getBusinessName() != null && request.getBusinessName().isBlank()) {
            throw new BadRequestException("Business name cannot be empty");
        }

        // Mise à jour des champs non-null
        if (request.getBusinessName() != null) {
            business.setBusinessName(request.getBusinessName());
        }
        if (request.getDescription() != null) {
            business.setDescription(request.getDescription());
        }
        if (request.getAddress() != null) {
            business.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            business.setCity(request.getCity());
        }
        if (request.getPostalCode() != null) {
            business.setPostalCode(request.getPostalCode());
        }
        if (request.getPhone() != null) {
            business.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            business.setEmail(request.getEmail());
        }
        if (request.getCategory() != null) {
            business.setCategory(Business.BusinessCategory.valueOf(request.getCategory()));
        }
        if (request.getLogoUrl() != null) {
            business.setLogoUrl(request.getLogoUrl());
        }

        business = businessRepository.save(business);
        log.info("Business updated successfully: {}", business.getId());

        return mapToResponse(business);
    }

    /**
     * Récupère les services actifs d'un business public
     */
    public List<ServiceResponse> getBusinessServices(String slug) {
        log.info("Getting services for business with slug: {}", slug);

        Business business = businessRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Business not found with slug: " + slug));

        if (!business.getIsActive()) {
            throw new BadRequestException("Business is not active");
        }

        List<com.booking.api.model.Service> services = serviceRepository.findByBusinessIdAndIsActiveTrue(business.getId());

        return services.stream()
                .map(this::mapServiceToResponse)
                .collect(Collectors.toList());
    }

    private ServiceResponse mapServiceToResponse(com.booking.api.model.Service service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .durationMinutes(service.getDurationMinutes())
                .price(service.getPrice())
                .color(service.getColor())
                .isActive(service.getIsActive())
                .displayOrder(service.getDisplayOrder())
                .createdAt(service.getCreatedAt())
                .updatedAt(service.getUpdatedAt())
                .build();
    }

    private BusinessResponse mapToResponse(Business business) {
        return BusinessResponse.builder()
                .id(business.getId())
                .businessName(business.getBusinessName())
                .slug(business.getSlug())
                .description(business.getDescription())
                .address(business.getAddress())
                .city(business.getCity())
                .postalCode(business.getPostalCode())
                .phone(business.getPhone())
                .email(business.getEmail())
                .category(business.getCategory() != null ? business.getCategory().name() : null)
                .logoUrl(business.getLogoUrl())
                .settings(business.getSettings())
                .isActive(business.getIsActive())
                .createdAt(business.getCreatedAt())
                .updatedAt(business.getUpdatedAt())
                .build();
    }
}
