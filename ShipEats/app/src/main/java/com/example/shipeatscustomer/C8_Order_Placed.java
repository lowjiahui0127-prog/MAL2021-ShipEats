package com.example.shipeatscustomer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class C8_Order_Placed extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_c8_order_placed);

        TextView orderNumberTv = findViewById(R.id.order_number);
        MaterialButton doneButton = findViewById(R.id.done_button);

        // Receive Order ID from Intent
        String orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId != null) {
            // Display only the last few characters if it's a long Firebase UID
            String displayId = orderId.length() > 8 ? orderId.substring(orderId.length() - 8).toUpperCase() : orderId;
            orderNumberTv.setText("#" + displayId);
        }

        doneButton.setOnClickListener(v -> {
            // Return to Menu Page and clear the stack
            Intent intent = new Intent(this, C3_Menu_Page.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}