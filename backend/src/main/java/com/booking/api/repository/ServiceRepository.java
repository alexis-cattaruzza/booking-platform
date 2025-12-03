package com.booking.api.repository;

import com.booking.api.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID> {

    Optional<Service> findByIdAndBusinessId(UUID id, UUID businessId);

    List<Service> findByBusinessIdAndIsActiveTrue(UUID businessId);

    List<Service> findByBusinessIdOrderByDisplayOrderAsc(UUID businessId);

    long countByBusinessIdAndIsActiveTrue(UUID businessId);

    List<Service> findByBusinessIdOrderByNameAsc(UUID businessId);
}
