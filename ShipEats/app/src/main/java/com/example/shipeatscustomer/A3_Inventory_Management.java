package com.example.shipeatscustomer;

import static java.util.Locale.filter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class A3_Inventory_Management extends AppCompatActivity {

    RecyclerView recyclerView;
    MaterialButton addItemBtn;
    EditText etSearch;
    DatabaseReference databaseRef;
    List<FoodItem> foodList;
    A3_InventoryAdapter adapter;

    // Launcher for image selection from gallery
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    AdminDialogHelper.handleImageResult(selectedImageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_a3_inventory_management);

        recyclerView = findViewById(R.id.inventory_recycler);
        addItemBtn = findViewById(R.id.btn_add_item);
        etSearch = findViewById(R.id.et_search);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        foodList = new ArrayList<>();

        databaseRef = FirebaseDatabase.getInstance().getReference("food_items");

        adapter = new A3_InventoryAdapter(this, foodList, new A3_InventoryAdapter.OnItemActionListener() {
            @Override
            public void onDelete(FoodItem item) {
                AdminDialogHelper.showDeleteConfirmDialog(A3_Inventory_Management.this, item.getId());
            }

            @Override
            public void onEdit(FoodItem item) {
                AdminDialogHelper.showEditMenuDialog(A3_Inventory_Management.this, imagePickerLauncher, item, false);
            }
        });

        recyclerView.setAdapter(adapter);

        // Real-time Firebase listener
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                foodList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    FoodItem item = data.getValue(FoodItem.class);
                    if (item != null) {
                        foodList.add(item);
                    }
                }
                adapter.notifyDataSetChanged();

                // If user is currently searching, maintain the filter even when data updates
                String currentSearch = etSearch.getText().toString();
                if (currentSearch.isEmpty()) {
                    adapter.updateList(foodList);
                } else {
                    filter(currentSearch);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(A3_Inventory_Management.this,
                        "Database Error", Toast.LENGTH_SHORT).show();
            }
        });

        // Search Bar Listener
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        addItemBtn.setOnClickListener(v -> AdminDialogHelper.showEditMenuDialog(this, imagePickerLauncher, null, true));

        setupBottomNav();

        // Highlight the Menu tab
        ImageView menuIcon = findViewById(R.id.inventory_icon);
        TextView menuText = findViewById(R.id.inventory_text);
        highlightCurrentTab(menuIcon, menuText);
    }

    // Search Filtering Logic
    private void filter(String text) {
        List<FoodItem> filteredList = new ArrayList<>();

        for (FoodItem item : foodList) {
            // Checks if name contains search text (case insensitive)
            if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }

        // Pass the filtered list to the adapter
        adapter.updateList(filteredList);
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
                    Toast.makeText(this, "You are already on Inventory", Toast.LENGTH_SHORT).show());

            footer.findViewById(R.id.orders_nav).setOnClickListener(v ->
                    startActivity(new Intent(this, A4_CustomerOrderActivity.class)));

            footer.findViewById(R.id.menu_nav).setOnClickListener(v ->
                    startActivity(new Intent(this, A5_MenuManagementActivity.class)));

            footer.findViewById(R.id.profile_nav).setOnClickListener(v ->
                    startActivity(new Intent(this, A6_Profile.class)));
        }
    }


}