package com.example.opp.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RoomType {

    private Long id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private int capacity;
    private String imageUrl;
    private LocalDateTime createdAt;

    public RoomType() {}

    public RoomType(String name, BigDecimal basePrice, int capacity) {
        this.name = name;
        this.basePrice = basePrice;
        this.capacity = capacity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getFormattedPrice() {
        return String.format("Rp %,.0f", basePrice);
    }
}
