package com.example.shipeatscustomer;

public class FoodItem {

    private String id;
    private String name;
    private String description;
    private String category;
    private double price;
    private int quantity;
    private String imageUrl;
    private String status;

    // Required empty constructor for Firebase
    public FoodItem() { }

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

    // ===== SETTERS =====

    public void setId(String id) { this.id = id; }

    public void setName(String name) { this.name = name; }

    public void setDescription(String description) { this.description = description; }

    public void setCategory(String category) { this.category = category; }

    public void setPrice(double price) { this.price = price; }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.status = calculateStatus(quantity); // auto update status
    }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public void setStatus(String status) { this.status = status; }
}