package com.v2r.v2rbackend.controller;

import com.v2r.v2rbackend.dto.OrderDTO;
import com.v2r.v2rbackend.entity.Order;
import com.v2r.v2rbackend.entity.OrderDetail;
import com.v2r.v2rbackend.entity.Subscription;
import com.v2r.v2rbackend.entity.User;
import com.v2r.v2rbackend.repository.SubscriptionRepository;
import com.v2r.v2rbackend.repository.UserRepository;
import com.v2r.v2rbackend.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Management", description = "CRUD operations for orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    // CREATE
    @PostMapping
    @Operation(summary = "Create a new order", description = "Create a new order in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        // Get authenticated user's email from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            User authenticatedUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            order.setUser(authenticatedUser);
        }
        
        Order createdOrder = orderService.createOrder(order);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    // CREATE from DTO
    @PostMapping("/dto")
    @Operation(summary = "Create a new order from DTO", description = "Create a new order using OrderDTO with userID, totalPrice, quantity, subscription")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<Order> createOrderFromDto(@RequestBody OrderDTO dto) {
        if (dto == null) {
            return ResponseEntity.badRequest().build();
        }
        if (dto.getUserID() == null || dto.getSubscription() == null || dto.getQuantity() == null || dto.getQuantity() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        User user = userRepository.findById(dto.getUserID())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + dto.getUserID()));

        Subscription subscription = subscriptionRepository.findById(dto.getSubscription())
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found with ID: " + dto.getSubscription()));

        // Compute price per unit from subscription price string if totalPrice not provided
        double pricePerUnit;
        try {
            // Subscription price is stored as string with dots/commas, normalize to digits
            String normalized = subscription.getPrice().replace(".", "").replace(",", "");
            pricePerUnit = Double.parseDouble(normalized);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        int quantity = dto.getQuantity();
        double calculatedTotal = pricePerUnit * quantity;
        double totalPrice = dto.getTotalPrice() != null ? dto.getTotalPrice() : calculatedTotal;

        // Build order entity
        Order order = new Order();
        order.setUser(user);
        order.setOrder_date(new Date());
        order.setTotalPrice(totalPrice);
        order.setStatus(1); // default Paid/Created

        // Build order detail
        OrderDetail detail = new OrderDetail();
        detail.setOrder(order);
        detail.setSubscription(subscription);
        detail.setQuantity(quantity);
        detail.setPricePerUnit(pricePerUnit);
        detail.setTotalPrice(pricePerUnit * quantity);

        // Attach to order (cascade saves detail)
        order.getOrderDetails().add(detail);

        Order created = orderService.createOrder(order);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // READ - Get all orders
    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieve all orders from the system")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.findAll();
        return ResponseEntity.ok(orders);
    }

    // READ - Get all orders with pagination
    @GetMapping("/paginated")
    @Operation(summary = "Get all orders with pagination", description = "Retrieve paginated list of orders")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<Page<Order>> getAllOrdersPaginated(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "orderID") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orders = orderService.findAll(pageable);
        return ResponseEntity.ok(orders);
    }

    // READ - Get order by ID
    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieve a specific order by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<Order> getOrderById(@PathVariable Integer id) {
        return orderService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // READ - Get orders by user ID
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get orders by user ID", description = "Retrieve all orders for a specific user")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable Integer userId) {
        List<Order> orders = orderService.findByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    // READ - Get orders by user ID with pagination
    @GetMapping("/user/{userId}/paginated")
    @Operation(summary = "Get orders by user ID with pagination", description = "Retrieve paginated orders for a specific user")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<Page<Order>> getOrdersByUserIdPaginated(
            @PathVariable Integer userId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "orderID") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orders = orderService.findByUserId(userId, pageable);
        return ResponseEntity.ok(orders);
    }

    // READ - Get orders by status
    @GetMapping("/status/{status}")
    @Operation(summary = "Get orders by status", description = "Retrieve all orders with a specific status")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable int status) {
        List<Order> orders = orderService.findByStatus(status);
        return ResponseEntity.ok(orders);
    }

    // READ - Get orders by status with pagination
    @GetMapping("/status/{status}/paginated")
    @Operation(summary = "Get orders by status with pagination", description = "Retrieve paginated orders with a specific status")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<Page<Order>> getOrdersByStatusPaginated(
            @PathVariable int status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "orderID") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orders = orderService.findByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }

    // UPDATE - Update entire order
    @PutMapping("/{id}")
    @Operation(summary = "Update an order", description = "Update an existing order by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order updated successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Order> updateOrder(
            @PathVariable Integer id,
            @RequestBody Order order
    ) {
        try {
            Order updatedOrder = orderService.updateOrder(id, order);
            return ResponseEntity.ok(updatedOrder);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // UPDATE - Update order status only
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status", description = "Update only the status of an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Integer id,
            @RequestParam int status
    ) {
        try {
            Order updatedOrder = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an order", description = "Delete an order by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, String>> deleteOrder(@PathVariable Integer id) {
        try {
            orderService.deleteOrder(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Order deleted successfully");
            response.put("orderId", String.valueOf(id));
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Business endpoints
    @GetMapping("/user/{userId}/total")
    @Operation(summary = "Calculate total amount for user", description = "Calculate total amount spent by a user")
    @ApiResponse(responseCode = "200", description = "Total calculated successfully")
    public ResponseEntity<Map<String, Object>> getTotalForUser(@PathVariable Integer userId) {
        double total = orderService.calculateTotalForUser(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("totalAmount", total);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/count")
    @Operation(summary = "Count orders for user", description = "Count total number of orders for a user")
    @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    public ResponseEntity<Map<String, Object>> countOrdersForUser(@PathVariable Integer userId) {
        long count = orderService.countOrdersByUser(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("orderCount", count);
        return ResponseEntity.ok(response);
    }
}