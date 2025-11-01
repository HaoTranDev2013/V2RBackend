package com.v2r.v2rbackend.repository;

import com.v2r.v2rbackend.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    
    // Find orders by user ID
    List<Order> findByUser_UserID(Integer userId);
    Page<Order> findByUser_UserID(Integer userId, Pageable pageable);
    
    // Find orders by status
    List<Order> findByStatus(int status);
    Page<Order> findByStatus(int status, Pageable pageable);
    
    // Find orders by user and status
    List<Order> findByUser_UserIDAndStatus(Integer userId, int status);
    Page<Order> findByUser_UserIDAndStatus(Integer userId, int status, Pageable pageable);
}
