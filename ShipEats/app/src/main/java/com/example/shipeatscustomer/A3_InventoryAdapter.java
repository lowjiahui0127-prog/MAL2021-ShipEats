package com.example.shipeatscustomer;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class A3_InventoryAdapter extends RecyclerView.Adapter<A3_InventoryAdapter.ViewHolder> {

    Context context;
    List<FoodItem> foodList;
    OnItemActionListener listener;

    public interface OnItemActionListener {
        void onDelete(FoodItem item);
        void onEdit(FoodItem item);
    }

    public A3_InventoryAdapter(Context context, List<FoodItem> foodList, OnItemActionListener listener) {
        this.context = context;
        this.foodList = foodList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.admin_inventory_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        FoodItem item = foodList.get(position);

        holder.tvName.setText(item.getName());
        holder.tvPrice.setText("RM " + String.format("%.2f", item.getPrice()));
        holder.tvQuantity.setText("Quantity: " + item.getQuantity());
        holder.tvStatus.setText(item.getStatus());

        // ✅ Status color
        switch (item.getStatus()) {
            case "Available":
                holder.tvStatus.setTextColor(Color.parseColor("#16A34A")); // Green
                break;
            case "Low Stock":
                holder.tvStatus.setTextColor(Color.parseColor("#F59E0B")); // Orange
                break;
            case "Sold Out":
                holder.tvStatus.setTextColor(Color.parseColor("#DC2626")); // Red
                break;
        }

        // ✅ Load Image
        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.no_image_available)
                .into(holder.ivFood);

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(item));
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPrice, tvQuantity, tvStatus;
        ImageView ivFood;
        MaterialButton btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_food_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvStatus = itemView.findViewById(R.id.tv_status);
            ivFood = itemView.findViewById(R.id.iv_food);

            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}