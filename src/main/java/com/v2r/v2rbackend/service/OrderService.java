package com.v2r.v2rbackend.service;

import com.v2r.v2rbackend.dto.CreateOrderRequest;
import com.v2r.v2rbackend.dto.OrderDetailResponse;
import com.v2r.v2rbackend.dto.OrderResponse;
import com.v2r.v2rbackend.entity.Order;
import com.v2r.v2rbackend.entity.OrderDetail;
import com.v2r.v2rbackend.entity.Subscription;
import com.v2r.v2rbackend.entity.User;
import com.v2r.v2rbackend.repository.OrderRepository;
import com.v2r.v2rbackend.repository.SubscriptionRepository;
import com.v2r.v2rbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class    OrderService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (req.getUserId() == null) {
            throw new IllegalArgumentException("userId is required");
        }

        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + req.getUserId()));

        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrder_date(new Date());
        order.setStatus(1);

        double orderTotal = 0.0;
        List<OrderDetail> details = new ArrayList<>();

        for (CreateOrderRequest.Item item : req.getItems()) {
            if (item.getSubscriptionId() == null) continue;
            Subscription subscription = subscriptionRepository.findById(item.getSubscriptionId())
                    .orElseThrow(() -> new IllegalArgumentException("Subscription not found: " + item.getSubscriptionId()));

            double pricePerUnit;
            try {
                // Use helper to robustly parse price strings like "190000", "190,000", or short forms like "199" -> 199000
                pricePerUnit = parsePriceString(subscription.getPrice());
            } catch (Exception e) {
                throw new IllegalStateException("Invalid subscription price for id=" + subscription.getId());
            }

            int quantity = (item.getQuantity() == null || item.getQuantity() <= 0) ? 1 : item.getQuantity();
            double totalPrice = pricePerUnit * quantity;

            OrderDetail od = new OrderDetail(order, subscription, quantity, pricePerUnit, totalPrice);
            details.add(od);
            orderTotal += totalPrice;
        }

        order.setTotalPrice(orderTotal);
        order.setOrderDetails(details);

        Order saved = orderRepository.save(order);

        return toResponse(saved);
    }

    /**
     * Parse a price string into a double (VND). Accepts values like:
     *  - "190000"
     *  - "190,000"
     *  - "190.000" (dots used as thousand separators)
     *  - "199" (interpreted as 199000 VND)
     *
     * Heuristic: if parsed numeric value is less than 1000, assume it's given in thousands and multiply by 1000.
     */
    private double parsePriceString(String priceStr) {
        if (priceStr == null) throw new IllegalArgumentException("price is null");
        // Remove common grouping characters and currency symbols, keep digits and dot
        String cleaned = priceStr.replaceAll("[\\s,₫$€£]", "");
        // If string contains dots and commas both, try to remove thousand separators (commas) and treat dot as decimal
        // But common case: "190.000" means 190000, so remove dots when they are used as thousand separators (no decimal part)
        // If cleaned contains more than one dot, remove all dots
        if (cleaned.chars().filter(ch -> ch == '.').count() > 1) {
            cleaned = cleaned.replace(".", "");
        } else if (cleaned.contains(".") && !cleaned.contains(",")) {
            // single dot - could be decimal separator or thousand separator. If characters after dot length == 3, treat as thousand separator
            int idx = cleaned.indexOf('.');
            if (cleaned.length() - idx - 1 == 3) {
                cleaned = cleaned.replace(".", "");
            }
        }

        double value = Double.parseDouble(cleaned);
        // Heuristic: if value looks like a short number (e.g. 199) assume it's in thousands -> multiply by 1000
        if (value > 0 && value < 1000) {
            value = value * 1000;
        }
        return value;
    }

    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(this::toResponse);
    }

    public Double getTotalPriceByStatus(int status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream()
                .mapToDouble(Order::getTotalPrice)
                .sum();
    }

    @Transactional
    public OrderResponse updateCheckCode(int orderId, String checkCode) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));
        order.setCheckCode(checkCode);
        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    @Transactional
    public OrderResponse updateStatus(int orderId, int status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));
        order.setStatus(status);
        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse res = new OrderResponse();
        res.setOrderID(order.getOrderID());
        res.setUserId(order.getUser() != null ? order.getUser().getUserID() : null);
        res.setUserEmail(order.getUser() != null ? order.getUser().getEmail() : null);
        res.setOrderDate(order.getOrder_date());
        res.setTotalPrice(order.getTotalPrice());
        res.setCheckCode(order.getCheckCode());
        res.setStatus(order.getStatus());
        List<OrderDetailResponse> list = order.getOrderDetails().stream().map(od -> {
            OrderDetailResponse dr = new OrderDetailResponse();
            dr.setOrderDetailId(od.getOrderDetailId());
            dr.setSubscriptionId(od.getSubscription() != null ? od.getSubscription().getId() : null);
            dr.setSubscriptionName(od.getSubscription() != null ? od.getSubscription().getName() : null);
            dr.setQuantity(od.getQuantity());
            dr.setPricePerUnit(od.getPricePerUnit());
            dr.setTotalPrice(od.getTotalPrice());
            return dr;
        }).collect(Collectors.toList());
        res.setOrderDetails(list);
        return res;
    }
}