package com.example.shipeatscustomer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class A4_OrderDetailPendingActivity extends AppCompatActivity {
    
    private TextView tvOrderNo, tvStatus, tvCustName, tvCustPhone, tvItems, tvTotal;
    private String orderId;
    private DatabaseReference orderRef;
    private AdminOrderModel currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a4_order_det_pending);

        orderId = getIntent().getStringExtra("orderId");
        if (orderId == null) {
            Toast.makeText(this, "Order ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        orderRef = FirebaseDatabase.getInstance().getReference("Orders").child(orderId);

        // Initialize Views with null checks
        tvOrderNo = findViewById(R.id.tvOrderNoValue);
        tvStatus = findViewById(R.id.tvStatusBadge);
        tvCustName = findViewById(R.id.tvCustName);
        tvCustPhone = findViewById(R.id.tvCustPhone);
        tvItems = findViewById(R.id.tvItemName);
        tvTotal = findViewById(R.id.tvTotalValue);

        loadOrderDetails();

        // ACCEPT ORDER: Change status to 'Preparing'
        View btnAccept = findViewById(R.id.btnPrimaryAction);
        if (btnAccept != null) {
            btnAccept.setOnClickListener(v -> {
                if (currentOrder == null) return;
                
                orderRef.child("status").setValue("Preparing")
                        .addOnSuccessListener(aVoid -> {
                            // Send notification to customer
                            NotificationHelper.sendOrderNotification(currentOrder.customerId, orderId, "Preparing");
                            
                            Intent intent = new Intent(this, OrderAcceptedPPActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            });
        }

        // CANCEL ORDER
        View btnCancel = findViewById(R.id.btnSecondaryAction);
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                if (currentOrder == null) return;

                orderRef.child("status").setValue("Cancelled")
                        .addOnSuccessListener(aVoid -> {
                            // Send notification to customer
                            NotificationHelper.sendOrderNotification(currentOrder.customerId, orderId, "Cancelled");
                            
                            AdminDialogHelper.showStatusDialog(this, R.layout.admin_dialog_order_deleted);
                            finish();
                        });
            });
        }

        View btnClose = findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> finish());
        }
    }

    private void loadOrderDetails() {
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentOrder = snapshot.getValue(AdminOrderModel.class);
                if (currentOrder != null) {
                    currentOrder.orderId = orderId; // Ensure ID is set
                    String displayId = orderId.length() > 8 ? orderId.substring(orderId.length() - 8).toUpperCase() : orderId;
                    if (tvOrderNo != null) tvOrderNo.setText("#" + displayId);
                    
                    if (tvStatus != null && currentOrder.status != null) {
                        tvStatus.setText(currentOrder.status.toLowerCase());
                    }
                    
                    if (tvCustName != null) {
                        tvCustName.setText(currentOrder.customerName != null ? currentOrder.customerName : "Guest");
                    }
                    
                    if (tvItems != null) {
                        tvItems.setText(currentOrder.items != null ? currentOrder.items : "No items");
                    }
                    
                    if (tvTotal != null) {
                        tvTotal.setText(currentOrder.totalPrice != null ? currentOrder.totalPrice : "RM 0.00");
                    }
                } else {
                    Toast.makeText(A4_OrderDetailPendingActivity.this, "Order details not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(A4_OrderDetailPendingActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}