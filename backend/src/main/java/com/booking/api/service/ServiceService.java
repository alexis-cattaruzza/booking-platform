package com.booking.api.service;

import com.booking.api.dto.request.ServiceRequest;
import com.booking.api.dto.response.ServiceResponse;
import com.booking.api.model.Business;
import com.booking.api.model.Service;
import com.booking.api.model.User;
import com.booking.api.repository.BusinessRepository;
import com.booking.api.repository.ServiceRepository;
import com.booking.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    /**
     * Récupère tous les services du business de l'utilisateur connecté
     */
    public List<ServiceResponse> getMyServices() {
        Business business = getAuthenticatedUserBusiness();
        log.info("Getting services for business: {}", business.getId());

        List<Service> services = serviceRepository.findByBusinessIdOrderByDisplayOrderAsc(business.getId());
        return services.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les services actifs d'un business public par slug
     */
    public List<ServiceResponse> getPublicServices(String businessSlug) {
        log.info("Getting public services for business slug: {}", businessSlug);

        Business business = businessRepository.findBySlug(businessSlug)
                .orElseThrow(() -> new RuntimeException("Business not found with slug: " + businessSlug));

        if (!business.getIsActive()) {
            throw new RuntimeException("Business is not active");
        }

        List<Service> services = serviceRepository.findByBusinessIdAndIsActiveTrue(business.getId());
        return services.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère un service par ID
     */
    public ServiceResponse getServiceById(UUID serviceId) {
        Business business = getAuthenticatedUserBusiness();

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        if (!service.getBusiness().getId().equals(business.getId())) {
            throw new RuntimeException("Service does not belong to your business");
        }

        return mapToResponse(service);
    }

    /**
     * Crée un nouveau service
     */
    @Transactional
    public ServiceResponse createService(ServiceRequest request) {
        Business business = getAuthenticatedUserBusiness();
        log.info("Creating new service for business: {}", business.getId());

        // Calculer le display order (dernier + 1)
        int nextOrder = (int) serviceRepository.countByBusinessIdAndIsActiveTrue(business.getId());

        Service service = Service.builder()
                .business(business)
                .name(request.getName())
                .description(request.getDescription())
                .durationMinutes(request.getDurationMinutes())
                .price(request.getPrice())
                .color(request.getColor() != null ? request.getColor() : "#3b82f6")
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : nextOrder)
                .build();

        service = serviceRepository.save(service);
        log.info("Service created successfully: {}", service.getId());

        return mapToResponse(service);
    }

    /**
     * Met à jour un service existant
     */
    @Transactional
    public ServiceResponse updateService(UUID serviceId, ServiceRequest request) {
        Business business = getAuthenticatedUserBusiness();

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        if (!service.getBusiness().getId().equals(business.getId())) {
            throw new RuntimeException("Service does not belong to your business");
        }

        log.info("Updating service: {}", serviceId);

        // Mise à jour des champs
        if (request.getName() != null) {
            service.setName(request.getName());
        }
        if (request.getDescription() != null) {
            service.setDescription(request.getDescription());
        }
        if (request.getDurationMinutes() != null) {
            service.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getPrice() != null) {
            service.setPrice(request.getPrice());
        }
        if (request.getColor() != null) {
            service.setColor(request.getColor());
        }
        if (request.getIsActive() != null) {
            service.setIsActive(request.getIsActive());
        }
        if (request.getDisplayOrder() != null) {
            service.setDisplayOrder(request.getDisplayOrder());
        }

        service = serviceRepository.save(service);
        log.info("Service updated successfully: {}", serviceId);

        return mapToResponse(service);
    }

    /**
     * Supprime un service (soft delete en le désactivant)
     */
    @Transactional
    public void deleteService(UUID serviceId) {
        Business business = getAuthenticatedUserBusiness();

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        if (!service.getBusiness().getId().equals(business.getId())) {
            throw new RuntimeException("Service does not belong to your business");
        }

        log.info("Deleting service: {}", serviceId);

        // Soft delete
        service.setIsActive(false);
        serviceRepository.save(service);

        log.info("Service deleted (deactivated) successfully: {}", serviceId);
    }

    private Business getAuthenticatedUserBusiness() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return businessRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Business not found for user"));
    }

    private ServiceResponse mapToResponse(Service service) {
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
}
