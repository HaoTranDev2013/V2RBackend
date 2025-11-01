package com.v2r.v2rbackend.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDTO {
    private String name;
    private boolean status;
    private String price;
    private int numberOfModel;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Integer getNumberOfModel() {
        return numberOfModel;
    }

    public void setNumberOfModel(Integer numberOfModel) {
        this.numberOfModel = numberOfModel;
    }
}
