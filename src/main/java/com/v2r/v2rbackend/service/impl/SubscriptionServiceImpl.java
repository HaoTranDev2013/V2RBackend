package com.v2r.v2rbackend.service.impl;

import com.v2r.v2rbackend.entity.Subscription;
import com.v2r.v2rbackend.repository.SubscriptionRepository;
import com.v2r.v2rbackend.service.SubscriptionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Override
    public List<Subscription> findAll() {
        return subscriptionRepository.findAll();
    }

    @Override
    public Page<Subscription> findAll(Pageable pageable) {
        return subscriptionRepository.findAll(pageable);
    }

    @Override
    public Optional<Subscription> findById(Integer id) {
        return subscriptionRepository.findById(id);
    }

    @Override
    public Subscription save(Subscription subscription) {
        if (subscriptionRepository.existsByName(subscription.getName())) {
            throw new IllegalArgumentException("Subscription with name '" + subscription.getName() + "' already exists");
        }
        return subscriptionRepository.save(subscription);
    }

    @Override
    public Subscription update(Integer id, Subscription subscription) {
        Subscription existingSubscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found with id: " + id));
        
        // Check if name is being changed to one that already exists
        if (!existingSubscription.getName().equals(subscription.getName()) 
                && subscriptionRepository.existsByName(subscription.getName())) {
            throw new IllegalArgumentException("Subscription with name '" + subscription.getName() + "' already exists");
        }
        
        existingSubscription.setName(subscription.getName());
        existingSubscription.setStatus(subscription.isStatus());
        existingSubscription.setPrice(subscription.getPrice());
        existingSubscription.setNumberOfModel(subscription.getNumberOfModel());
        
        return subscriptionRepository.save(existingSubscription);
    }

    @Override
    public void deleteById(Integer id) {
        if (!subscriptionRepository.existsById(id)) {
            throw new EntityNotFoundException("Subscription not found with id: " + id);
        }
        subscriptionRepository.deleteById(id);
    }

    @Override
    public Optional<Subscription> findByName(String name) {
        return subscriptionRepository.findByName(name);
    }

    @Override
    public List<Subscription> findByStatus(boolean status) {
        return subscriptionRepository.findByStatus(status);
    }

    @Override
    public Page<Subscription> findByStatus(boolean status, Pageable pageable) {
        return subscriptionRepository.findByStatus(status, pageable);
    }

    @Override
    public boolean existsByName(String name) {
        return subscriptionRepository.existsByName(name);
    }
}
