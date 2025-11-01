package com.v2r.v2rbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subscriptions")
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean status;

    @Column(nullable = false)
    private String price;

    @Column(name = "number_of_model", nullable = true)
    private Integer numberOfModel ; // -1 represents unlimited/infinity

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    /**
     * Check if this subscription has unlimited models
     * @return true if numberOfModel is -1 (unlimited), false otherwise
     */
    public boolean isUnlimitedModels() {
        return numberOfModel != null && numberOfModel == -1;
    }

    /**
     * Get display string for number of models
     * @return "Unlimited" if -1, otherwise the number as string
     */
    public String getNumberOfModelDisplay() {
        if (numberOfModel == null || numberOfModel == -1) {
            return "Unlimited";
        }
        return numberOfModel.toString();
    }
}
