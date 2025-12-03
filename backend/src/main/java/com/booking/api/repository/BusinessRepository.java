package com.booking.api.repository;

import com.booking.api.model.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessRepository extends JpaRepository<Business, UUID> {

    Optional<Business> findBySlug(String slug);

    Optional<Business> findByUserId(UUID userId);

    boolean existsBySlug(String slug);

    @Query("SELECT b FROM Business b WHERE b.user.email = :email")
    Optional<Business> findByEmail(@Param("email") String email);
}
