package com.v2r.v2rbackend.repository;

import com.v2r.v2rbackend.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    
    // Find order details by order ID
    List<OrderDetail> findByOrder_OrderID(Integer orderId);
    
    // Find order details by subscription ID
    List<OrderDetail> findBySubscription_Id(Integer subscriptionId);
}
