package com.v2r.v2rbackend.service;

import com.v2r.v2rbackend.entity.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SubscriptionService {
    List<Subscription> findAll();
    Page<Subscription> findAll(Pageable pageable);
    Optional<Subscription> findById(Integer id);
    Subscription save(Subscription subscription);
    Subscription update(Integer id, Subscription subscription);
    void deleteById(Integer id);
    Optional<Subscription> findByName(String name);
    List<Subscription> findByStatus(boolean status);
    Page<Subscription> findByStatus(boolean status, Pageable pageable);
    boolean existsByName(String name);
}
