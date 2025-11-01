package com.v2r.v2rbackend.service.impl;

import com.v2r.v2rbackend.entity.Order;
import com.v2r.v2rbackend.repository.OrderRepository;
import com.v2r.v2rbackend.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public Order createOrder(Order order) {
        // Validate user is present
        if (order.getUser() == null) {
            logger.error("Cannot create order: User is null");
            throw new IllegalArgumentException("Order must have a user associated with it");
        }
        
        logger.info("Creating new order for user ID: {}", order.getUser().getUserID());
        
        if (order.getOrder_date() == null) {
            order.setOrder_date(new Date());
        }
        
        if (order.getStatus() == 0) {
            order.setStatus(1); // Default to 'Paid' status
        }
        
        Order savedOrder = orderRepository.save(order);
        logger.info("Order created successfully with ID: {}", savedOrder.getOrderID());
        
        return savedOrder;
    }

    @Override
    public List<Order> findAll() {
        logger.info("Fetching all orders");
        return orderRepository.findAll();
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        logger.info("Fetching all orders with pagination");
        return orderRepository.findAll(pageable);
    }

    @Override
    public Optional<Order> findById(Integer id) {
        logger.info("Fetching order by ID: {}", id);
        return orderRepository.findById(id);
    }

    @Override
    public List<Order> findByUserId(Integer userId) {
        logger.info("Fetching orders for user ID: {}", userId);
        return orderRepository.findByUser_UserID(userId);
    }

    @Override
    public Page<Order> findByUserId(Integer userId, Pageable pageable) {
        logger.info("Fetching orders for user ID: {} with pagination", userId);
        return orderRepository.findByUser_UserID(userId, pageable);
    }

    @Override
    public List<Order> findByStatus(int status) {
        logger.info("Fetching orders by status: {}", status);
        return orderRepository.findByStatus(status);
    }

    @Override
    public Page<Order> findByStatus(int status, Pageable pageable) {
        logger.info("Fetching orders by status: {} with pagination", status);
        return orderRepository.findByStatus(status, pageable);
    }

    @Override
    public Order updateOrder(Integer id, Order orderDetails) {
        logger.info("Updating order ID: {}", id);
        
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        
        // Update fields
        if (orderDetails.getTotalPrice() != 0) {
            order.setTotalPrice(orderDetails.getTotalPrice());
        }
        
        if (orderDetails.getStatus() != 0) {
            order.setStatus(orderDetails.getStatus());
        }
        
        if (orderDetails.getUser() != null) {
            order.setUser(orderDetails.getUser());
        }
        
        Order updatedOrder = orderRepository.save(order);
        logger.info("Order updated successfully: {}", updatedOrder.getOrderID());
        
        return updatedOrder;
    }

    @Override
    public Order updateOrderStatus(Integer id, int status) {
        logger.info("Updating order ID: {} status to: {}", id, status);
        
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        
        logger.info("Order status updated successfully");
        return updatedOrder;
    }

    @Override
    public void deleteOrder(Integer id) {
        logger.info("Deleting order ID: {}", id);
        
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        
        orderRepository.delete(order);
        logger.info("Order deleted successfully: {}", id);
    }

    @Override
    public double calculateTotalForUser(Integer userId) {
        logger.info("Calculating total amount for user ID: {}", userId);
        
        List<Order> orders = orderRepository.findByUser_UserID(userId);
        double total = orders.stream()
                .mapToDouble(Order::getTotalPrice)
                .sum();
        
        logger.info("Total amount for user {}: {}", userId, total);
        return total;
    }

    @Override
    public long countOrdersByUser(Integer userId) {
        logger.info("Counting orders for user ID: {}", userId);
        
        long count = orderRepository.findByUser_UserID(userId).size();
        
        logger.info("Total orders for user {}: {}", userId, count);
        return count;
    }
}
