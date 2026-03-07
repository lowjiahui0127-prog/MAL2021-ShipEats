package com.example.shipeatscustomer;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<CartItem> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addToCart(FoodItem foodItem, int quantity, String specialInstructions, boolean wantCutlery) {
        for (CartItem item : cartItems) {
            if (item.getFoodItem().getId().equals(foodItem.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                // Update instructions if any new ones provided, or append
                if (specialInstructions != null && !specialInstructions.isEmpty()) {
                    item.setSpecialInstructions(specialInstructions);
                }
                item.setWantCutlery(wantCutlery);
                return;
            }
        }
        cartItems.add(new CartItem(foodItem, quantity, specialInstructions, wantCutlery));
    }

    public void addToCart(FoodItem foodItem, int quantity) {
        addToCart(foodItem, quantity, "", false);
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void clearCart() {
        cartItems.clear();
    }

    public int getCartCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }
}