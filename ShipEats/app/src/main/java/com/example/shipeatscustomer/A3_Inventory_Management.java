package com.example.shipeatscustomer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.net.Uri;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

    DatabaseReference databaseRef;
    List<FoodItem> foodList;
    A3_InventoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_a3_inventory_management);

        recyclerView = findViewById(R.id.inventory_recycler);
        addItemBtn = findViewById(R.id.btn_add_item);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        foodList = new ArrayList<>();

        databaseRef = FirebaseDatabase.getInstance().getReference("food_items");

        adapter = new A3_InventoryAdapter(this, foodList, new A3_InventoryAdapter.OnItemActionListener() {
            @Override
            public void onDelete(FoodItem item) {
                showDeleteDialog(item);
            }

            @Override
            public void onEdit(FoodItem item) {
                showItemDialog(item);
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(A3_Inventory_Management.this,
                        "Database Error", Toast.LENGTH_SHORT).show();
            }
        });

        addItemBtn.setOnClickListener(v -> showItemDialog(null));

        setupBottomNav();
    }

    private void setupBottomNav() {
        findViewById(R.id.dashboard_nav).setOnClickListener(v ->
                startActivity(new Intent(this, A2_Dashboard.class)));

        findViewById(R.id.profile_nav).setOnClickListener(v ->
                startActivity(new Intent(this, A6_Profile.class)));
    }

    // ===================== ADD & EDIT DIALOG =====================
    private void showItemDialog(FoodItem item) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.admin_dialog_add_item, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        TextView title = view.findViewById(R.id.dialog_title);
        MaterialButton btnConfirm = view.findViewById(R.id.btn_add_confirm);

        EditText etName = view.findViewById(R.id.et_name);
        EditText etDesc = view.findViewById(R.id.et_description);
        EditText etPrice = view.findViewById(R.id.et_price);
        Spinner spinnerQuantity = view.findViewById(R.id.spinner_quantity);
        Spinner spinnerCategory = view.findViewById(R.id.spinner_category);

        // Close button
        view.findViewById(R.id.btn_close).setOnClickListener(v -> dialog.dismiss());

        // Quantity Spinner 0–50
        List<Integer> quantityList = new ArrayList<>();
        for (int i = 0; i <= 50; i++) {
            quantityList.add(i);
        }

        ArrayAdapter<Integer> quantityAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, quantityList);
        spinnerQuantity.setAdapter(quantityAdapter);

        // Category Spinner
        String[] categories = {"Main Dish", "Beverage", "Snack", "Dessert"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(categoryAdapter);

        // ================= EDIT MODE =================
        if (item != null) {

            title.setText("Edit Food Item");
            btnConfirm.setText("Update Item");

            etName.setText(item.getName());
            etDesc.setText(item.getDescription());
            etPrice.setText(String.valueOf(item.getPrice()));

            spinnerQuantity.setSelection(item.getQuantity());

            int categoryPosition = categoryAdapter.getPosition(item.getCategory());
            spinnerCategory.setSelection(categoryPosition);

            btnConfirm.setOnClickListener(v -> {

                String name = etName.getText().toString().trim();
                String description = etDesc.getText().toString().trim();
                double price = Double.parseDouble(etPrice.getText().toString());
                int quantity = (int) spinnerQuantity.getSelectedItem();
                String category = spinnerCategory.getSelectedItem().toString();

                FoodItem updatedItem = new FoodItem(
                        item.getId(),
                        name,
                        description,
                        category,
                        price,
                        quantity,
                        item.getImageUrl()
                );

                databaseRef.child(item.getId()).setValue(updatedItem);

                dialog.dismiss();
                showSuccessDialog("Item updated successfully");
            });

        }
        // ================= ADD MODE =================
        else {

            title.setText("Add New Food Item");
            btnConfirm.setText("Add Item");

            btnConfirm.setOnClickListener(v -> {

                String name = etName.getText().toString().trim();
                String description = etDesc.getText().toString().trim();
                String priceText = etPrice.getText().toString().trim();

                if (name.isEmpty() || priceText.isEmpty()) {
                    Toast.makeText(this,
                            "Please fill all required fields",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                double price = Double.parseDouble(priceText);
                int quantity = (int) spinnerQuantity.getSelectedItem();
                String category = spinnerCategory.getSelectedItem().toString();

                String id = databaseRef.push().getKey();

                FoodItem newItem = new FoodItem(
                        id,
                        name,
                        description,
                        category,
                        price,
                        quantity,
                        ""
                );

                databaseRef.child(id).setValue(newItem);

                dialog.dismiss();
                showSuccessDialog("Item added successfully");
            });
        }
    }

    // ================= DELETE =================
    private void showDeleteDialog(FoodItem item) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Delete", (dialog, which) ->
                        databaseRef.child(item.getId()).removeValue())
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ================= SUCCESS POPUP =================
    private void showSuccessDialog(String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.admin_dialog_success, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        ((TextView) view.findViewById(R.id.tv_success_message)).setText(message);

        view.postDelayed(dialog::dismiss, 1500);
    }
}