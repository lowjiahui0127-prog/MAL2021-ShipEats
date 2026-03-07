package com.example.shipeatscustomer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class C7_Shopping_Cart extends AppCompatActivity {

    private LinearLayout cartItemsContainer;
    private TextView subtotalTv, taxTv, feeTv, totalTv;
    private double platformFee = 1.00;
    private MaterialButton continueToPaymentButton;
    private SwitchMaterial preOrderSwitch;
    private EditText pickupTimeInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_c7_shopping_cart);
        
        ImageView back_button = findViewById(R.id.back_button);
        preOrderSwitch = findViewById(R.id.pre_order);
        LinearLayout preOrderSection = findViewById(R.id.enable_pre_order);
        pickupTimeInput = findViewById(R.id.pickup_time_input);
        cartItemsContainer = findViewById(R.id.shopping_cart_items);
        continueToPaymentButton = findViewById(R.id.continue_to_payment_button);
        
        subtotalTv = findViewById(R.id.subtotal);
        taxTv = findViewById(R.id.service_tax);
        feeTv = findViewById(R.id.platform_fee);
        totalTv = findViewById(R.id.total_price);

        if (back_button != null) back_button.setOnClickListener(v -> finish());

        // IF THE PRE-ORDER TOGGLE IS ON, SHOW THE TIME SELECTION
        if (preOrderSwitch != null) {
            preOrderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (preOrderSection != null) preOrderSection.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            });
        }

        if (continueToPaymentButton != null) {
            continueToPaymentButton.setOnClickListener(v -> {
                String pickupTime = "";
                if (preOrderSwitch != null && preOrderSwitch.isChecked()) {
                    pickupTime = pickupTimeInput.getText().toString().trim();
                    if (pickupTime.isEmpty()) {
                        Toast.makeText(this, "Please enter a pick-up time for pre-order", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // Navigate to Payment Method Selection
                Intent intent = new Intent(this, C11_Payment_Method.class);
                intent.putExtra("PICKUP_TIME", pickupTime);
                startActivity(intent);
            });
        }

        loadCartItems();
    }

    private void loadCartItems() {
        cartItemsContainer.removeAllViews();
        List<CartItem> items = CartManager.getInstance().getCartItems();

        if (items.isEmpty()) {
            checkCartEmpty();
            return;
        }

        for (CartItem item : items) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.shopping_cart_items_layout, cartItemsContainer, false);
            
            TextView name = itemView.findViewById(R.id.item_name);
            TextView price = itemView.findViewById(R.id.item_price);
            TextView counter = itemView.findViewById(R.id.food_counter);
            ImageView image = itemView.findViewById(R.id.item_image);
            ImageView plus = itemView.findViewById(R.id.plus_button);
            ImageView minus = itemView.findViewById(R.id.minus_button);
            MaterialButton btnRemove = itemView.findViewById(R.id.btn_remove);

            FoodItem food = item.getFoodItem();
            if (name != null) name.setText(food.getName());
            if (price != null) price.setText("RM " + String.format("%.2f", food.getPrice() * item.getQuantity()));
            if (counter != null) counter.setText(String.valueOf(item.getQuantity()));
            if (image != null) Glide.with(this).load(food.getImageUrl()).placeholder(R.drawable.no_image_available).into(image);

            if (plus != null) {
                plus.setOnClickListener(v -> {
                    item.setQuantity(item.getQuantity() + 1);
                    updateItemUI(item, counter, price);
                    calculateTotal();
                });
            }

            if (minus != null) {
                minus.setOnClickListener(v -> {
                    if (item.getQuantity() > 1) {
                        item.setQuantity(item.getQuantity() - 1);
                        updateItemUI(item, counter, price);
                    } else {
                        CartManager.getInstance().getCartItems().remove(item);
                        cartItemsContainer.removeView(itemView);
                        checkCartEmpty();
                    }
                    calculateTotal();
                });
            }

            if (btnRemove != null) {
                btnRemove.setOnClickListener(v -> {
                    CartManager.getInstance().getCartItems().remove(item);
                    cartItemsContainer.removeView(itemView);
                    calculateTotal();
                    checkCartEmpty();
                });
            }

            cartItemsContainer.addView(itemView);
        }

        calculateTotal();
        checkCartEmpty();
    }

    private void updateItemUI(CartItem item, TextView counter, TextView price) {
        if (counter != null) counter.setText(String.valueOf(item.getQuantity()));
        if (price != null) price.setText("RM " + String.format("%.2f", item.getFoodItem().getPrice() * item.getQuantity()));
    }

    private void calculateTotal() {
        double subtotal = 0;
        for (CartItem item : CartManager.getInstance().getCartItems()) {
            subtotal += item.getFoodItem().getPrice() * item.getQuantity();
        }

        double tax = subtotal * 0.06; // 6% Service Tax
        double total = subtotal + tax + platformFee;

        if (subtotalTv != null) subtotalTv.setText("RM " + String.format("%.2f", subtotal));
        if (taxTv != null) taxTv.setText("RM " + String.format("%.2f", tax));
        if (feeTv != null) feeTv.setText("RM " + String.format("%.2f", platformFee));
        if (totalTv != null) totalTv.setText("RM " + String.format("%.2f", total));
    }

    private void checkCartEmpty() {
        TextView emptyMessage = findViewById(R.id.empty_cart_message);
        LinearLayout preOrderContainer = findViewById(R.id.pre_order_container);
        LinearLayout summaryContainer = findViewById(R.id.checkout_summary_container);

        boolean isEmpty = CartManager.getInstance().getCartItems().isEmpty();

        if (emptyMessage != null) emptyMessage.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (preOrderContainer != null) preOrderContainer.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        if (summaryContainer != null) summaryContainer.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (continueToPaymentButton != null) {
            if (isEmpty) {
                continueToPaymentButton.setEnabled(false);
                continueToPaymentButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#c7c7c7")));
            } else {
                continueToPaymentButton.setEnabled(true);
                continueToPaymentButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E69303")));
            }
        }
    }
}