package com.example.shipeatscustomer;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminDialogHelper {
    private static DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("food_items");
    private static Uri selectedImageUri = null;
    private static ImageView dialogImageView = null;

    public static void showEditMenuDialog(AppCompatActivity activity, ActivityResultLauncher<Intent> launcher, FoodItem item, boolean isNew) {
        Dialog dialog = new Dialog(activity);

        // FIX FOR DIALOG SIZE
        View view = LayoutInflater.from(activity).inflate(R.layout.admin_dialog_add_item, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
            lp.width = (int) (screenWidth * 0.90);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = android.view.Gravity.CENTER;

            dialog.getWindow().setAttributes(lp);
        }

        dialog.show();

        // Initialize UI Elements
        EditText etName = view.findViewById(R.id.et_name);
        EditText etDesc = view.findViewById(R.id.et_description);
        EditText etPrice = view.findViewById(R.id.et_price);
        EditText etQuantity = view.findViewById(R.id.et_quantity);
        MaterialButton btnPlus = view.findViewById(R.id.btn_plus);
        MaterialButton btnMinus = view.findViewById(R.id.btn_minus);
        Spinner spinnerCat = view.findViewById(R.id.spinner_category);
//        CheckBox cbVeg = view.findViewById(R.id.cb_vegetarian);
//        CheckBox cbSpicy = view.findViewById(R.id.cb_spicy);
        MaterialButton btnConfirm = view.findViewById(R.id.btn_add_confirm);
        ImageView btnClose = view.findViewById(R.id.btn_close);
        dialogImageView = view.findViewById(R.id.iv_food_preview);

        selectedImageUri = null;

        // Quantity Button Logic
        btnPlus.setOnClickListener(v -> {
            int current = etQuantity.getText().toString().isEmpty() ? 0 : Integer.parseInt(etQuantity.getText().toString());
            etQuantity.setText(String.valueOf(current + 1));
        });

        btnMinus.setOnClickListener(v -> {
            int current = etQuantity.getText().toString().isEmpty() ? 0 : Integer.parseInt(etQuantity.getText().toString());
            if (current > 0) etQuantity.setText(String.valueOf(current - 1));
        });

        // Category Spinner Setup
        ArrayAdapter<CharSequence> catAdapter = ArrayAdapter.createFromResource(activity,
                R.array.category_array, android.R.layout.simple_spinner_item);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCat.setAdapter(catAdapter);

        // Populate Data if in Edit Mode
        if (!isNew && item != null) {
            etName.setText(item.getName());
            etDesc.setText(item.getDescription());
            etPrice.setText(String.valueOf(item.getPrice()));
            etQuantity.setText(String.valueOf(item.getQuantity()));
//            cbVeg.setChecked(item.isVegetarian());
//            cbSpicy.setChecked(item.isSpicy());

            int catPosition = catAdapter.getPosition(item.getCategory());
            spinnerCat.setSelection(catPosition);

            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(activity).load(item.getImageUrl()).into(dialogImageView);
            }
        } else {
            etQuantity.setText("1"); // Default for new item
        }

        // Image Picker Trigger
        dialogImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            launcher.launch(intent);
        });

        btnConfirm.setOnClickListener(v -> {
            try {
                String name = etName.getText().toString().trim();
                String desc = etDesc.getText().toString().trim();
                String priceTxt = etPrice.getText().toString().trim();
                String qtyTxt = etQuantity.getText().toString().trim();

                if (name.isEmpty() || priceTxt.isEmpty()) {
                    Toast.makeText(activity, "Fill required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                String id = isNew ? databaseRef.push().getKey() : item.getId();
                double price = Double.parseDouble(priceTxt);
                int quantity = Integer.parseInt(qtyTxt);
                String category = spinnerCat.getSelectedItem().toString();
//                boolean isVeg = cbVeg.isChecked();
//                boolean isSpicy = cbSpicy.isChecked();

                String imageUrl = (selectedImageUri != null) ? selectedImageUri.toString() :
                                 (item != null && item.getImageUrl() != null ? item.getImageUrl() : "");

                FoodItem updatedItem = new FoodItem(id, name, desc, category, price, quantity, imageUrl);
//                updatedItem.setVegetarian(isVeg);
//                updatedItem.setSpicy(isSpicy);

                databaseRef.child(id).setValue(updatedItem).addOnSuccessListener(aVoid -> {
                    dialog.dismiss();
                    showStatusDialog(activity, isNew ? R.layout.admin_dialog_menu_add : R.layout.admin_dialog_menu_complete);
                });
            } catch (NumberFormatException e) {
                Toast.makeText(activity, "Invalid number format", Toast.LENGTH_SHORT).show();
            }
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public static void handleImageResult(Uri uri) {
        selectedImageUri = uri;
        if (dialogImageView != null && uri != null) {
            Glide.with(dialogImageView.getContext()).load(uri).into(dialogImageView);
        }
    }

    public static void showDeleteConfirmDialog(Context context, String itemId) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.admin_dialog_item_confirm_delete);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button btnDelete = dialog.findViewById(R.id.btnDelete);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        btnDelete.setOnClickListener(v -> {
            databaseRef.child(itemId).removeValue().addOnSuccessListener(aVoid -> {
                dialog.dismiss();
                showStatusDialog(context, R.layout.admin_dialog_menu_delete);
            });
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public static void showStatusDialog(Context context, int layoutId) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(layoutId);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        new android.os.Handler(Looper.getMainLooper()).postDelayed(dialog::dismiss, 1500);
    }
}