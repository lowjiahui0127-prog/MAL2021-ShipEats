package com.example.shipeatscustomer;

import java.io.Serializable;

public class AdminOrderModel implements Serializable {
    public String orderId;
    public String items;
    public String totalPrice;
    public String status;
    public String customerName;
    public String customerId;
    public Long itemCount;
    public String date;
    public String paymentMethod;
    public String pickupTime; // New field for pre-orders

    public AdminOrderModel() {
        // Required empty constructor for Firebase
    }

    public AdminOrderModel(String orderId, String items, String totalPrice, String status, Long itemCount, String customerName, String customerId, String date, String paymentMethod, String pickupTime) {
        this.orderId = orderId;
        this.items = items;
        this.totalPrice = totalPrice;
        this.status = status;
        this.itemCount = itemCount;
        this.customerName = customerName;
        this.customerId = customerId;
        this.date = date;
        this.paymentMethod = paymentMethod;
        this.pickupTime = pickupTime;
    }
}