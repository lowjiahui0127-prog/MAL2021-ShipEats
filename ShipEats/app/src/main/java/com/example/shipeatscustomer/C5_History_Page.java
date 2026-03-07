package com.example.shipeatscustomer;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class C5_History_Page extends AppCompatActivity {

    private LinearLayout historyContainer;
    private DatabaseReference ordersRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_c5_history_page);

        historyContainer = findViewById(R.id.history_lists_container);
        ordersRef = FirebaseDatabase.getInstance().getReference("Orders");
        
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            currentUserId = "GuestUser";
        }

        ImageView history_icon = findViewById(R.id.history_icon);
        TextView history_text = findViewById(R.id.history_text);
        ImageView back_button = findViewById(R.id.back_button);
        LinearLayout menu_nav = findViewById(R.id.menu_nav);
        LinearLayout settings_nav = findViewById(R.id.settings_nav);

        if (back_button != null) back_button.setOnClickListener(v -> finish());
        
        if (menu_nav != null) {
            menu_nav.setOnClickListener(v -> {
                Intent intent = new Intent(this, C3_Menu_Page.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            });
        }
        
        if (settings_nav != null) {
            settings_nav.setOnClickListener(v -> {
                Intent intent = new Intent(this, C13_Settings_Page.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            });
        }

        // Highlight Footer
        if (history_icon != null && history_text != null) {
            int activeColor = Color.parseColor("#FFD700");
            history_icon.setColorFilter(activeColor);
            history_text.setTextColor(activeColor);
            history_text.setTypeface(null, Typeface.BOLD);
        }

        loadOrderHistory();
    }

    private void loadOrderHistory() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    return;
                }

                historyContainer.removeAllViews();
                int count = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    AdminOrderModel order = ds.getValue(AdminOrderModel.class);
                    
                    if (order != null && order.customerId != null && order.customerId.equals(currentUserId)) {
                        addHistoryRow(order);
                        count++;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(C5_History_Page.this, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addHistoryRow(AdminOrderModel order) {
        // Using the row layout that matches the table design
        View row = LayoutInflater.from(this).inflate(R.layout.order_history_card_layout, historyContainer, false);
        
        TextView tvDate = row.findViewById(R.id.date_history);
        TextView tvPayment = row.findViewById(R.id.payment_history);
        TextView tvAmount = row.findViewById(R.id.amount_history);
        TextView btnDetails = row.findViewById(R.id.view_details_button);

        if (tvDate != null) tvDate.setText(order.date != null ? order.date : "Today");
        if (tvPayment != null) tvPayment.setText(order.paymentMethod != null ? order.paymentMethod : "Card");
        if (tvAmount != null) tvAmount.setText(order.totalPrice != null ? order.totalPrice.replace("RM ", "") : "0.00");
        
        if (btnDetails != null) {
            btnDetails.setOnClickListener(v -> {
                Intent intent = new Intent(this, C6_History_Details_Page.class);
                intent.putExtra("ORDER_ID", order.orderId);
                startActivity(intent);
            });
        }

        historyContainer.addView(row);
    }
}
