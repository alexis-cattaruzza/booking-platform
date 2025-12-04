package com.booking.api.service;

import com.booking.api.dto.request.AppointmentRequest;
import com.booking.api.dto.response.AppointmentResponse;
import com.booking.api.exception.BadRequestException;
import com.booking.api.exception.ConflictException;
import com.booking.api.exception.NotFoundException;
import com.booking.api.model.Appointment;
import com.booking.api.model.Business;
import com.booking.api.model.Customer;
import com.booking.api.repository.AppointmentRepository;
import com.booking.api.repository.BusinessRepository;
import com.booking.api.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final BusinessRepository businessRepository;
    private final ServiceRepository serviceRepository;
    private final CustomerService customerService;
    private final EmailService emailService;

    /**
     * Create a new appointment (public booking)
     * Uses pessimistic locking to prevent double-booking
     */
    @Transactional
    public AppointmentResponse createAppointment(String businessSlug, AppointmentRequest request) {        // Get business
        Business business = businessRepository.findBySlug(businessSlug)
                .orElseThrow(() -> new NotFoundException("Business not found"));

        // Get service
        com.booking.api.model.Service service = serviceRepository
                .findByIdAndBusinessId(request.getServiceId(), business.getId())
                .orElseThrow(() -> new NotFoundException("Service not found"));

        if (!service.getIsActive()) {
            throw new RuntimeException("Service is not active");
        }

        // Validate appointment datetime is in the future
        if (request.getAppointmentDatetime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot book appointment in the past");
        }

        // Calculate appointment end time
        LocalDateTime appointmentStart = request.getAppointmentDatetime();
        LocalDateTime appointmentEnd = appointmentStart.plusMinutes(service.getDurationMinutes());

        // Check for conflicts using pessimistic locking
        // This ensures no concurrent booking can create a conflict
        List<Appointment> existingAppointments = appointmentRepository
                .findActiveAppointmentsForLocking(
                        business.getId(),
                        appointmentStart.minusMinutes(service.getDurationMinutes()),
                        appointmentEnd
                );

        // Check for any overlap with existing appointments
        for (Appointment existing : existingAppointments) {
            LocalDateTime existingStart = existing.getAppointmentDatetime();
            LocalDateTime existingEnd = existingStart.plusMinutes(existing.getDurationMinutes());

            // Check for overlap: start1 < end2 AND start2 < end1
            if (appointmentStart.isBefore(existingEnd) &&
                    existingStart.isBefore(appointmentEnd)) {
                throw new ConflictException("This time slot is no longer available");
            }
        }

        // Find or create customer
        Customer customer = customerService.findOrCreateCustomer(business, request.getCustomer());

        // Generate cancellation token
        String cancellationToken = UUID.randomUUID().toString();

        // Create appointment
        Appointment appointment = Appointment.builder()
                .business(business)
                .service(service)
                .customer(customer)
                .appointmentDatetime(appointmentStart)
                .durationMinutes(service.getDurationMinutes())
                .price(service.getPrice())
                .status(Appointment.AppointmentStatus.PENDING)
                .notes(request.getNotes())
                .cancellationToken(cancellationToken)
                .build();

        appointment = appointmentRepository.save(appointment);

        // Update customer stats
        customer.setTotalAppointments(customer.getTotalAppointments() + 1);
        customer.setLastAppointmentAt(LocalDateTime.now());

        // Send confirmation email
        emailService.sendBookingConfirmation(appointment);

        return toAppointmentResponse(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getBusinessAppointments(
            UUID businessId,
            LocalDateTime start,
            LocalDateTime end) {

        return appointmentRepository
                .findByBusinessIdAndAppointmentDatetimeBetweenOrderByAppointmentDatetimeAsc(
                        businessId, start, end)
                .stream()
                .map(this::toAppointmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse updateAppointmentStatus(
            UUID appointmentId,
            Appointment.AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        appointment.setStatus(newStatus);
        appointment = appointmentRepository.save(appointment);

        return toAppointmentResponse(appointment);
    }

    @Transactional
    public void cancelAppointment(String cancellationToken) {
        Appointment appointment = appointmentRepository.findByCancellationToken(cancellationToken)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        if (appointment.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
            throw new ConflictException("Appointment is already cancelled");
        }

        if (appointment.getStatus() == Appointment.AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel a completed appointment");
        }

        // Check if appointment is in the past
        if (appointment.getAppointmentDatetime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot cancel a past appointment");
        }

        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        // Send cancellation email
        emailService.sendCancellationEmail(appointment);
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentByToken(String token) {
        Appointment appointment = appointmentRepository.findByCancellationToken(token)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));
        return toAppointmentResponse(appointment);
    }

    private AppointmentResponse toAppointmentResponse(Appointment appointment) {
        AppointmentResponse.ServiceInfo serviceInfo = AppointmentResponse.ServiceInfo.builder()
                .id(appointment.getService().getId())
                .name(appointment.getService().getName())
                .durationMinutes(appointment.getService().getDurationMinutes())
                .price(appointment.getService().getPrice())
                .color(appointment.getService().getColor())
                .build();

        AppointmentResponse.CustomerInfo customerInfo = AppointmentResponse.CustomerInfo.builder()
                .id(appointment.getCustomer().getId())
                .firstName(appointment.getCustomer().getFirstName())
                .lastName(appointment.getCustomer().getLastName())
                .email(appointment.getCustomer().getEmail())
                .phone(appointment.getCustomer().getPhone())
                .build();

        return AppointmentResponse.builder()
                .id(appointment.getId())
                .appointmentDatetime(appointment.getAppointmentDatetime())
                .durationMinutes(appointment.getDurationMinutes())
                .price(appointment.getPrice())
                .status(appointment.getStatus())
                .notes(appointment.getNotes())
                .cancellationToken(appointment.getCancellationToken())
                .service(serviceInfo)
                .customer(customerInfo)
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}
