package com.example.shipeatscustomer;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class A5_MenuAdapter extends RecyclerView.Adapter<A5_MenuAdapter.MenuViewHolder> {
    private Context context;
    private List<FoodItem> menuList;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public A5_MenuAdapter(Context context, List<FoodItem> menuList, ActivityResultLauncher<Intent> launcher) {
        this.context = context;
        this.menuList = menuList;
        this.imagePickerLauncher = launcher;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.admin_it_menuitem, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        FoodItem item = menuList.get(position);
        holder.tvName.setText(item.getName());
        holder.tvPrice.setText("RM " + String.format("%.2f", item.getPrice()));
        holder.swPublish.setChecked(item.isPublished());

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context).load(item.getImageUrl()).into(holder.imgFood);
        }

        // Handle Publish Toggle
        holder.swPublish.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setPublished(isChecked);
            FirebaseDatabase.getInstance().getReference("menu_items")
                    .child(item.getId()).child("published").setValue(isChecked);
        });

        // Handle Edit Flow
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof AppCompatActivity) {
                AdminDialogHelper.showEditMenuDialog((AppCompatActivity) context, imagePickerLauncher, item, false);
            }
        });

        // Handle Remove from Menu Flow
        holder.btnRemove.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference("menu_items")
                    .child(item.getId()).removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Removed from menu", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() { return menuList.size(); }

    public static class MenuViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;
        Button btnRemove;
        ImageView imgFood;
        SwitchCompat swPublish;
        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFoodName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            imgFood = itemView.findViewById(R.id.imgFood);
            swPublish = itemView.findViewById(R.id.swPublish);
        }
    }
}