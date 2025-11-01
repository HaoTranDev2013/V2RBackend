package com.v2r.v2rbackend.service;

import com.v2r.v2rbackend.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    
    // Create
    Order createOrder(Order order);
    
    // Read
    List<Order> findAll();
    Page<Order> findAll(Pageable pageable);
    Optional<Order> findById(Integer id);
    List<Order> findByUserId(Integer userId);
    Page<Order> findByUserId(Integer userId, Pageable pageable);
    List<Order> findByStatus(int status);
    Page<Order> findByStatus(int status, Pageable pageable);
    
    // Update
    Order updateOrder(Integer id, Order order);
    Order updateOrderStatus(Integer id, int status);
    
    // Delete
    void deleteOrder(Integer id);
    
    // Business methods
    double calculateTotalForUser(Integer userId);
    long countOrdersByUser(Integer userId);
}
