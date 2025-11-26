package com.booking.api.repository;

import com.booking.api.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByBusinessId(UUID businessId);

    Optional<Subscription> findByStripeCustomerId(String stripeCustomerId);

    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);
}
