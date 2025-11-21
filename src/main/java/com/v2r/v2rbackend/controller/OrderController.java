package com.v2r.v2rbackend.controller;

import com.v2r.v2rbackend.dto.CreateOrderRequest;
import com.v2r.v2rbackend.dto.OrderResponse;
import com.v2r.v2rbackend.dto.UpdateCheckCodeRequest;
import com.v2r.v2rbackend.dto.UpdateStatusRequest;
import com.v2r.v2rbackend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Sort by orderID descending (newest first - higher ID = newer order)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderID"));
        Page<OrderResponse> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/total-price/status/{status}")
    public ResponseEntity<Double> getTotalPriceByStatus(@PathVariable int status) {
        Double totalPrice = orderService.getTotalPriceByStatus(status);
        return ResponseEntity.ok(totalPrice);
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/checkcode")
    public ResponseEntity<OrderResponse> updateCheckCode(@PathVariable Integer id, @RequestBody UpdateCheckCodeRequest req) {
        if (id == null || req == null) {
            return ResponseEntity.badRequest().build();
        }
        OrderResponse res = orderService.updateCheckCode(id, req.getCheckCode());
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Integer id, @RequestBody UpdateStatusRequest req) {
        if (id == null || req == null || req.getStatus() == null) {
            return ResponseEntity.badRequest().build();
        }
        OrderResponse res = orderService.updateStatus(id, req.getStatus());
        return ResponseEntity.ok(res);
    }
}