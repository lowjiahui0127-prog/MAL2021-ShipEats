package com.example.shipeatscustomer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class C4_Food_Details extends AppCompatActivity {

    private String foodId;
    private FoodItem currentFood;
    private int quantity = 1;

    private ImageView foodImage;
    private TextView tvName, tvPrice, tvQuantity, tvDescription, tvCounter;
    private EditText etSpecialInstructions;
    private SwitchMaterial swCutlery;
    private TextView tagSpicy, tagVeg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_c4_food_details);

        foodId = getIntent().getStringExtra("FOOD_ID");

        // Initialize Views
        foodImage = findViewById(R.id.detail_food_image);
        tvName = findViewById(R.id.food_name);
        tvPrice = findViewById(R.id.food_price);
        tvQuantity = findViewById(R.id.food_quantity);
        tvDescription = findViewById(R.id.food_description);
        tvCounter = findViewById(R.id.food_counter);
        etSpecialInstructions = findViewById(R.id.special_instructions_input);
        swCutlery = findViewById(R.id.want_cutlery);
        tagSpicy = findViewById(R.id.tag_spicy);
        tagVeg = findViewById(R.id.tag_veg);

        ImageView btnPlus = findViewById(R.id.plus_button);
        ImageView btnMinus = findViewById(R.id.minus_button);
        MaterialButton btnAddToCart = findViewById(R.id.add_to_cart_button);
        ImageView btnClose = findViewById(R.id.close_food_details);

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> finish());
        }

        // Counter Logic
        if (btnPlus != null) {
            btnPlus.setOnClickListener(v -> {
                quantity++;
                tvCounter.setText(String.valueOf(quantity));
            });
        }

        if (btnMinus != null) {
            btnMinus.setOnClickListener(v -> {
                if (quantity > 1) {
                    quantity--;
                    tvCounter.setText(String.valueOf(quantity));
                }
            });
        }

        if (foodId != null) {
            loadFoodDetails();
        }

        if (btnAddToCart != null) {
            btnAddToCart.setOnClickListener(v -> {
                if (currentFood != null) {
                    String instructions = etSpecialInstructions.getText().toString().trim();
                    boolean wantCutlery = swCutlery.isChecked();
                    
                    CartManager.getInstance().addToCart(currentFood, quantity, instructions, wantCutlery);
                    Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }

    private void loadFoodDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("menu_items").child(foodId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentFood = snapshot.getValue(FoodItem.class);
                if (currentFood != null) {
                    currentFood.setId(snapshot.getKey());
                    tvName.setText(currentFood.getName());
                    tvPrice.setText("RM " + String.format("%.2f", currentFood.getPrice()));
                    tvQuantity.setText("Quantity: " + currentFood.getQuantity());
                    tvDescription.setText(currentFood.getDescription() != null && !currentFood.getDescription().isEmpty() ? 
                            currentFood.getDescription() : "No description available.");
                    
                    // Show/Hide Dietary Tags
                    if (tagSpicy != null) {
                        tagSpicy.setVisibility(currentFood.isSpicy() ? View.VISIBLE : View.GONE);
                    }
                    if (tagVeg != null) {
                        tagVeg.setVisibility(currentFood.isVegetarian() ? View.VISIBLE : View.GONE);
                    }

                    Glide.with(C4_Food_Details.this)
                            .load(currentFood.getImageUrl())
                            .placeholder(R.drawable.no_image_available)
                            .into(foodImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(C4_Food_Details.this, "Error loading details", Toast.LENGTH_SHORT).show();
            }
        });
    }
}