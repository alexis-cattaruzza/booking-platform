package com.booking.api.repository;

import com.booking.api.model.Notification;
import com.booking.api.model.Notification.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByAppointmentId(UUID appointmentId);

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByStatusOrderByCreatedAtAsc(NotificationStatus status);
}
