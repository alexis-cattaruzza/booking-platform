package com.booking.api.repository;

import com.booking.api.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    List<Customer> findByBusinessId(UUID businessId);

    Optional<Customer> findByIdAndBusinessId(UUID id, UUID businessId);

    Optional<Customer> findByBusinessIdAndEmail(UUID businessId, String email);

    Optional<Customer> findByBusinessIdAndPhone(UUID businessId, String phone);

    boolean existsByBusinessIdAndEmail(UUID businessId, String email);

    List<Customer> findByBusinessIdOrderByLastNameAsc(UUID businessId);

    @Query("SELECT c FROM Customer c WHERE c.business.id = :businessId " +
           "AND (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR c.phone LIKE CONCAT('%', :search, '%'))")
    List<Customer> searchCustomers(@Param("businessId") UUID businessId, @Param("search") String search);

    long countByBusinessId(UUID businessId);

    Optional<Customer> findByEmail(String email);
}
