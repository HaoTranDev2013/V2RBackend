package com.v2r.v2rbackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.subscription")
public class SubscriptionConfig {
    
    // Free tier configuration
    private int freeModelLimit = 3; // Default: 3 models for free users
    
    public int getFreeModelLimit() {
        return freeModelLimit;
    }
    
    public void setFreeModelLimit(int freeModelLimit) {
        this.freeModelLimit = freeModelLimit;
    }
}
