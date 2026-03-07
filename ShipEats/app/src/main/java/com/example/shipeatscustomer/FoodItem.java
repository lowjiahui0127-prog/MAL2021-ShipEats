package com.example.shipeatscustomer;

import java.io.Serializable;

public class FoodItem implements Serializable {

    private String id;
    private String name;
    private String description;
    private String category;
    private double price;
    private int quantity;
    private String imageUrl;
    private String status;
    
    // Fields for filtering and details
    private boolean vegetarian;
    private boolean spicy;
    private double rating;
    private boolean published; // New field to control visibility in customer menu

    // Required empty constructor for Firebase
    public FoodItem() { 
        this.rating = 5.0; // Default rating
        this.published = true; // Default to visible
    }

    public FoodItem(String id, String name, String description,
                    String category, double price,
                    int quantity, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.status = calculateStatus(quantity);
        this.rating = 5.0;
        this.published = true;
    }

    private String calculateStatus(int quantity) {
        if (quantity == 0) return "Sold Out";
        else if (quantity <= 3) return "Low Stock";
        else return "Available";
    }

    // ===== GETTERS =====
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getImageUrl() { return imageUrl; }
    public String getStatus() { return status; }
    public boolean isVegetarian() { return vegetarian; }
    public boolean isSpicy() { return spicy; }
    public double getRating() { return rating; }
    public boolean isPublished() { return published; }

    // ===== SETTERS =====
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.status = calculateStatus(quantity);
    }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setStatus(String status) { this.status = status; }
    public void setVegetarian(boolean vegetarian) { this.vegetarian = vegetarian; }
    public void setSpicy(boolean spicy) { this.spicy = spicy; }
    public void setRating(double rating) { this.rating = rating; }
    public void setPublished(boolean published) { this.published = published; }
}