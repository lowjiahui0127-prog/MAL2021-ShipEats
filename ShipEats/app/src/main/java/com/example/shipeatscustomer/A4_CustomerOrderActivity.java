package com.example.shipeatscustomer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class A4_CustomerOrderActivity extends AppCompatActivity {
    private static final String TAG = "AdminOrders";

    private TabLayout tabLayout;
    private RecyclerView rvOrders;
    private AdminOrdersAdapter adapter;
    private List<AdminOrderModel> allOrders = new ArrayList<>();
    private List<AdminOrderModel> filteredList = new ArrayList<>();
    private String currentStatus = "All";
    private TextInputEditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a4_cust_order);

        // Ensure admin is still authenticated
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, A1_Login_Page.class));
            finish();
            return;
        }

        tabLayout = findViewById(R.id.tabLayout);
        rvOrders = findViewById(R.id.rvOrders);
        searchInput = findViewById(R.id.search_input);

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminOrdersAdapter(this, filteredList);
        rvOrders.setAdapter(adapter);

        loadAllOrders();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab != null && tab.getText() != null) {
                    currentStatus = tab.getText().toString();
                    applyFilters();
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        if (searchInput != null) {
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applyFilters();
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        setupBottomNav();

        // Highlight the Menu tab
        ImageView menuIcon = findViewById(R.id.orders_icon);
        TextView menuText = findViewById(R.id.orders_text);
        highlightCurrentTab(menuIcon, menuText);
    }

    private void loadAllOrders() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allOrders.clear();
                Log.d(TAG, "Snapshot children count: " + snapshot.getChildrenCount());

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Log.d(TAG, "Order key: " + ds.getKey() + " -> " + ds.getValue());

                    AdminOrderModel order = ds.getValue(AdminOrderModel.class);
                    if (order != null) {
                        order.orderId = ds.getKey();
                        allOrders.add(order);
                        Log.d(TAG, "✅ Added order: " + order.orderId + " | status: " + order.status);
                    } else {
                        Log.e(TAG, "❌ Order is null for key: " + ds.getKey());
                    }
                }

                // Show all orders initially (for debugging)
                filteredList.clear();
                filteredList.addAll(allOrders);
                adapter.notifyDataSetChanged();

                if (filteredList.isEmpty()) {
                    Toast.makeText(A4_CustomerOrderActivity.this,
                            "No orders found. Check database structure.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(A4_CustomerOrderActivity.this,
                            "Loaded " + filteredList.size() + " orders", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(A4_CustomerOrderActivity.this,
                        "Error loading orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        String query = (searchInput != null && searchInput.getText() != null) ?
                searchInput.getText().toString().toLowerCase().trim() : "";
        filteredList.clear();

        for (AdminOrderModel order : allOrders) {
            if (order == null || order.status == null) continue;

            // Normalize status: "Completed" -> "Done" for tab matching
            String statusForTab = order.status;
            if ("Completed".equalsIgnoreCase(statusForTab)) statusForTab = "Done";

            boolean matchesStatus = "All".equalsIgnoreCase(currentStatus) ||
                    currentStatus.equalsIgnoreCase(statusForTab);

            String orderId = order.orderId != null ? order.orderId.toLowerCase() : "";
            String custName = order.customerName != null ? order.customerName.toLowerCase() : "";

            boolean matchesSearch = orderId.contains(query) || custName.contains(query);

            if (matchesStatus && matchesSearch) {
                filteredList.add(order);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void highlightCurrentTab(ImageView icon, TextView text) {
        if (icon != null && text != null) {
            int activeColor = Color.parseColor("#FFD700");
            icon.setColorFilter(activeColor);
            text.setTextColor(activeColor);
            text.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }

    private void setupBottomNav() {
        View footer = findViewById(R.id.footer_section);
        if (footer != null) {
            footer.findViewById(R.id.dashboard_nav).setOnClickListener(v ->
                    startActivity(new Intent(this, A2_Dashboard.class)));

            footer.findViewById(R.id.inventory_nav).setOnClickListener(v ->
                    startActivity(new Intent(this, A3_Inventory_Management.class)));

            footer.findViewById(R.id.orders_nav).setOnClickListener(v ->
                    Toast.makeText(this, "Already on Orders", Toast.LENGTH_SHORT).show());

            footer.findViewById(R.id.menu_nav).setOnClickListener(v ->
                    startActivity(new Intent(this, A5_MenuManagementActivity.class)));

            footer.findViewById(R.id.profile_nav).setOnClickListener(v ->
                    startActivity(new Intent(this, A6_Profile.class)));
        }
    }
}
