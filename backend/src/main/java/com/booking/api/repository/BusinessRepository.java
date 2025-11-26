package com.booking.api.repository;

import com.booking.api.model.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessRepository extends JpaRepository<Business, UUID> {

    Optional<Business> findBySlug(String slug);

    Optional<Business> findByUserId(UUID userId);

    boolean existsBySlug(String slug);
}
