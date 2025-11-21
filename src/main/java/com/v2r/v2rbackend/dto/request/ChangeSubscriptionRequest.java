package com.v2r.v2rbackend.dto.request;

public class ChangeSubscriptionRequest {
    
    private Integer userId;
    private Integer subscriptionId;

    public ChangeSubscriptionRequest() {
    }

    public ChangeSubscriptionRequest(Integer userId, Integer subscriptionId) {
        this.userId = userId;
        this.subscriptionId = subscriptionId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Integer subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
}
