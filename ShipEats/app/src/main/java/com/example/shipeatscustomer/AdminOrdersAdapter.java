package com.example.shipeatscustomer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminOrdersAdapter extends RecyclerView.Adapter<AdminOrdersAdapter.OrderViewHolder> {

    private final Context context;
    private final List<AdminOrderModel> orderList;

    public AdminOrdersAdapter(Context context, List<AdminOrderModel> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.admin_it_order_card, parent, false);
        return new OrderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        AdminOrderModel order = orderList.get(position);
        if (order == null) return;

        // Order number
        String orderId = order.orderId != null ? order.orderId : "unknown";
        String displayId = orderId.length() > 8 ? orderId.substring(orderId.length() - 8).toUpperCase() : orderId;
        holder.tvOrderNo.setText("#" + displayId);

        // Status
        String status = order.status != null ? order.status : "Pending";
        holder.tvStatusBadge.setText(status.toLowerCase());

        // Set badge color based on status
        if ("Pending".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#FFA500")); // orange
            holder.tvStatusBadge.setTextColor(Color.WHITE);
        } else if ("Preparing".equalsIgnoreCase(status)) {
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#4CAF50")); // green
            holder.tvStatusBadge.setTextColor(Color.WHITE);
        } else {
            holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#2196F3")); // blue
            holder.tvStatusBadge.setTextColor(Color.WHITE);
        }

        // Pre-order / Pickup Time Display
        if (order.pickupTime != null && !order.pickupTime.isEmpty()) {
            holder.tvPickupTime.setText("Pickup: " + order.pickupTime);
            holder.tvPickupTime.setVisibility(View.VISIBLE);
        } else {
            holder.tvPickupTime.setVisibility(View.GONE);
        }

        // Customer name
        String customerName = order.customerName != null ? order.customerName : "Guest";
        holder.tvCustomerName.setText("⚪ " + customerName);

        // Items
        String itemsText = order.items != null ? order.items.replace("\n", ", ") : "No items";
        holder.tvItems.setText("Items: " + itemsText);

        // Item count
        long count = order.itemCount;
        holder.tvItemCount.setText(count + (count > 1 ? " items" : " item"));

        // Total price
        String total = order.totalPrice != null ? order.totalPrice : "RM 0.00";
        holder.tvTotal.setText("Total " + total);

        // Click to open order details
        holder.itemView.setOnClickListener(v -> {
            Intent intent;
            if ("Pending".equalsIgnoreCase(status)) {
                intent = new Intent(context, A4_OrderDetailPendingActivity.class);
            } else if ("Preparing".equalsIgnoreCase(status)) {
                intent = new Intent(context, A4_OrderDetailPreparingActivity.class);
            } else {
                intent = new Intent(context, A4_OrderDetailCompletedActivity.class);
            }
            
            intent.putExtra("orderId", orderId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNo, tvStatusBadge, tvCustomerName, tvItems, tvItemCount, tvTotal, tvPickupTime;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderNo = itemView.findViewById(R.id.tvOrderNo);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvPickupTime = itemView.findViewById(R.id.tvPickupTime);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvItems = itemView.findViewById(R.id.tvItems);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);
            tvTotal = itemView.findViewById(R.id.tvTotal);
        }
    }
}