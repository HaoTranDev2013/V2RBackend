package com.v2r.v2rbackend.dto;

/**
 * OrderDTO is a minimal request/response model for creating or transferring order data.
 * Fields:
 * - userID: ID of the user who owns the order
 * - totalPrice: total price of the order
 * - quantity: number of months (or items) for the subscription in this order
 * - subscription: ID of the subscription plan
 */
public class OrderDTO {

    private Integer userID;
    private Double totalPrice;
    private Integer quantity;
    private Integer subscription; // subscription plan ID

    public OrderDTO() {
    }

    public OrderDTO(Integer userID, Double totalPrice, Integer quantity, Integer subscription) {
        this.userID = userID;
        this.totalPrice = totalPrice;
        this.quantity = quantity;
        this.subscription = subscription;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getSubscription() {
        return subscription;
    }

    public void setSubscription(Integer subscription) {
        this.subscription = subscription;
    }
}
