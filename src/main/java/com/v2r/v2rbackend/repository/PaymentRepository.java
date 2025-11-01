package com.v2r.v2rbackend.repository;

import com.v2r.v2rbackend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    
    // Find payment by transaction code
    Optional<Payment> findByTransactionCode(String transactionCode);
    
    // Find payment by order ID
    Optional<Payment> findByOrder_OrderID(Integer orderId);
}
