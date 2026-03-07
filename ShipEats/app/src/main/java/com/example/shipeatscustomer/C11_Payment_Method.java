package com.example.shipeatscustomer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class C11_Payment_Method extends AppCompatActivity {

    private MaterialCardView btnCard, btnFPX;
    private TextView tvPaymentInfo;
    private ImageView ivBack;
    private Button btnPlaceOrder;
    private DatabaseReference ordersRef;
    private String selectedMethod = "Card"; // Default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_c11_payment_method);

        ordersRef = FirebaseDatabase.getInstance().getReference("Orders");

        // 1. Initialize Views
        btnCard = findViewById(R.id.btnCardInfo);
        btnFPX = findViewById(R.id.btnFPXInfo);
        tvPaymentInfo = findViewById(R.id.tvPaymentInfo);
        ivBack = findViewById(R.id.ivBack);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        // 2. Back Button Logic
        ivBack.setOnClickListener(v -> finish());

        // 3. Set Card Click Logic
        btnCard.setOnClickListener(v -> {
            selectedMethod = "Card";
            showCardInfo();
        });

        // 4. Set FPX Click Logic
        btnFPX.setOnClickListener(v -> {
            selectedMethod = "FPX";
            showFPXInfo();
        });

        // 5. Place Order Logic
        btnPlaceOrder.setOnClickListener(v -> placeOrder());

        // Load default state
        showCardInfo();
    }

    private void placeOrder() {
        List<CartItem> cartItems = CartManager.getInstance().getCartItems();
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user != null ? user.getUid() : "GuestUser";
        String userName = user != null && user.getEmail() != null ? user.getEmail().split("@")[0] : "Guest Customer";
        
        String orderId = ordersRef.push().getKey();
        String pickupTime = getIntent().getStringExtra("PICKUP_TIME");
        if (pickupTime == null) pickupTime = "";
        
        StringBuilder itemsSummary = new StringBuilder();
        double subtotal = 0;
        int totalQty = 0;
        
        for (CartItem item : cartItems) {
            itemsSummary.append(item.getFoodItem().getName())
                        .append(" x")
                        .append(item.getQuantity());
            
            if (item.getSpecialInstructions() != null && !item.getSpecialInstructions().isEmpty()) {
                itemsSummary.append("\n [Note: ").append(item.getSpecialInstructions()).append("]");
            }
            if (item.isWantCutlery()) {
                itemsSummary.append("\n [Want Cutlery: Yes]");
            }
            itemsSummary.append("\n\n");
            
            subtotal += item.getFoodItem().getPrice() * item.getQuantity();
            totalQty += item.getQuantity();
        }

        double total = subtotal + (subtotal * 0.06) + 1.00;
        String finalPrice = String.format("RM %.2f", total);
        
        // Current Date
        String date = new SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(new Date());

        AdminOrderModel order = new AdminOrderModel(
                orderId, 
                itemsSummary.toString().trim(), 
                finalPrice, 
                "Pending", 
                (long) totalQty, 
                userName, 
                userId,
                date,
                selectedMethod,
                pickupTime
        );

        if (orderId != null) {
            ordersRef.child(orderId).setValue(order).addOnSuccessListener(aVoid -> {
                CartManager.getInstance().clearCart();
                notifyAdminsAndCustomer(order, userId);
                Intent intent = new Intent(C11_Payment_Method.this, C8_Order_Placed.class);
                intent.putExtra("ORDER_ID", orderId);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to place order", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void showCardInfo() {
        btnCard.setCardBackgroundColor(Color.parseColor("#B2EBF2")); 
        btnFPX.setCardBackgroundColor(Color.WHITE);
        tvPaymentInfo.setText("Secure credit/debit card payment.");
    }

    private void showFPXInfo() {
        btnFPX.setCardBackgroundColor(Color.parseColor("#B2EBF2"));
        btnCard.setCardBackgroundColor(Color.WHITE);
        tvPaymentInfo.setText("Pay directly via your bank's secure online portal.");
    }

    private void notifyAdminsAndCustomer(AdminOrderModel order, String customerId) {
        NotificationHelper.sendOrderNotification(customerId, order.orderId, "Pending");
        DatabaseReference adminsRef = FirebaseDatabase.getInstance().getReference("Admins");
        adminsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot adminSnap : snapshot.getChildren()) {
                    NotificationHelper.sendAdminNotification(adminSnap.getKey(), "New Order", "Order #" + order.orderId, "order", order.orderId);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}