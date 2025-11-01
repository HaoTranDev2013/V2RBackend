package com.v2r.v2rbackend.repository;

import com.v2r.v2rbackend.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    Optional<Subscription> findByName(String name);
    List<Subscription> findByStatus(boolean status);
    Page<Subscription> findByStatus(boolean status, Pageable pageable);
    boolean existsByName(String name);
}
