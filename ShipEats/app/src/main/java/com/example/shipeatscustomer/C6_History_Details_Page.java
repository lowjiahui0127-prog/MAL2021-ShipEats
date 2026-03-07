package com.example.shipeatscustomer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class C6_History_Details_Page extends AppCompatActivity {

    private String orderId;
    private DatabaseReference orderRef;
    
    private TextView tvOrderNum, tvOrderDate, tvSubtotal, tvTax, tvFee, tvTotal, tvPaymentStatus, tvPickupStatus;
    private LinearLayout itemListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_c6_history_details_page);
        
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        orderId = getIntent().getStringExtra("ORDER_ID");
        
        // Initialize Views
        ImageView backButton = findViewById(R.id.back_button);
        tvOrderNum = findViewById(R.id.order_num);
        tvOrderDate = findViewById(R.id.order_date);
        tvSubtotal = findViewById(R.id.sub_total);
        tvTax = findViewById(R.id.service_tax);
        tvFee = findViewById(R.id.platform_fee);
        tvTotal = findViewById(R.id.total_cost);
        tvPaymentStatus = findViewById(R.id.payment_status);
        tvPickupStatus = findViewById(R.id.pickup_status);
        itemListContainer = findViewById(R.id.order_item_list);

        // Back Button Logic
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Setup Footer Navigation
        setupBottomNav();

        if (orderId != null) {
            orderRef = FirebaseDatabase.getInstance().getReference("Orders").child(orderId);
            loadOrderDetails();
        } else {
            Toast.makeText(this, "Order ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadOrderDetails() {
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                AdminOrderModel order = snapshot.getValue(AdminOrderModel.class);
                if (order != null) {
                    String orderIdFromDb = order.orderId != null ? order.orderId : orderId;
                    String displayId = orderIdFromDb.length() > 8 ? orderIdFromDb.substring(orderIdFromDb.length() - 8).toUpperCase() : orderIdFromDb;
                    tvOrderNum.setText("#" + displayId);
                    
                    // Since we didn't store date yet in previous steps, we can hide or show placeholder
                    tvOrderDate.setText("Today"); 
                    
                    tvTotal.setText(order.totalPrice);
                    tvPickupStatus.setText(order.status);
                    
                    // Basic item list display (since items is stored as a concatenated string)
                    TextView itemsTv = new TextView(C6_History_Details_Page.this);
                    itemsTv.setText(order.items);
                    itemsTv.setTextSize(16); // Fixed: '16sp' is not valid Java syntax
                    itemsTv.setPadding(10, 10, 10, 10);
                    itemListContainer.addView(itemsTv);
                    
                    // Set status colors
                    if ("Completed".equalsIgnoreCase(order.status)) {
                        tvPickupStatus.setTextColor(Color.BLUE);
                        tvPaymentStatus.setText("PAID");
                        tvPaymentStatus.setTextColor(Color.GREEN);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(C6_History_Details_Page.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNav() {
        View menuNav = findViewById(R.id.menu_nav);
        View historyNav = findViewById(R.id.history_nav);
        View settingsNav = findViewById(R.id.settings_nav);

        if (menuNav != null) {
            menuNav.setOnClickListener(v -> {
                Intent intent = new Intent(this, C3_Menu_Page.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            });
        }


        if (historyNav != null) {
            historyNav.setOnClickListener(v -> finish()); // Go back to history list
        }

        if (settingsNav != null) {
            settingsNav.setOnClickListener(v -> {
                Intent intent = new Intent(this, C13_Settings_Page.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            });
        }
    }
}