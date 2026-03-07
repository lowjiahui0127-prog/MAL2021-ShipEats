package com.example.shipeatscustomer;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class C3_Menu_Page extends AppCompatActivity {

    private LinearLayout foodCardContainer;
    private TextView noMenuText;
    private DatabaseReference menuRef = FirebaseDatabase.getInstance().getReference("menu_items");
    private List<FoodItem> fullMenuList = new ArrayList<>();
    private DrawerLayout drawerLayout;

    // Filter Views
    private CheckBox cbQty, cbRatings, cbPricing;
    private EditText etMinPrice, etMaxPrice;
    private CheckBox cbVeg, cbNonVeg, cbSpicy, cbNonSpicy;
    private CheckBox cbMain, cbSides, cbDrinks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_c3_menu_page);

        foodCardContainer = findViewById(R.id.food_card_container);
        noMenuText = findViewById(R.id.no_menu_text);
        drawerLayout = findViewById(R.id.drawer_layout);

        ImageView menu_icon = findViewById(R.id.menu_icon);
        TextView menu_text = findViewById(R.id.menu_text);
        LinearLayout history_nav = findViewById(R.id.history_nav);
        LinearLayout settings_nav = findViewById(R.id.settings_nav);
        ImageView shopping_cart = findViewById(R.id.shopping_cart_icon);
        ImageView notification = findViewById(R.id.notification_icon);
        LinearLayout filter_icon = findViewById(R.id.filter_icon);
        
        View filterView = findViewById(R.id.filter_drawer_container);
        TextView close_tab_button = filterView.findViewById(R.id.close_tab_button);
        com.google.android.material.search.SearchBar searchBar = findViewById(R.id.search_bar);

        // Initialize Filter Components
        cbQty = filterView.findViewById(R.id.quantity_checkbox);
        cbRatings = filterView.findViewById(R.id.ratings_checkbox);
        cbPricing = filterView.findViewById(R.id.pricing_checkbox);
        etMinPrice = filterView.findViewById(R.id.min_price);
        etMaxPrice = filterView.findViewById(R.id.max_price);
        cbVeg = filterView.findViewById(R.id.vegetarian_checkbox);
        cbNonVeg = filterView.findViewById(R.id.non_vegetarian_checkbox);
        cbSpicy = filterView.findViewById(R.id.spicy_checkbox);
        cbNonSpicy = filterView.findViewById(R.id.non_spicy_checkbox);
        cbMain = filterView.findViewById(R.id.main_checkbox);
        cbSides = filterView.findViewById(R.id.sides_checkbox);
        cbDrinks = filterView.findViewById(R.id.drinks_checkbox);
        MaterialButton btnApply = filterView.findViewById(R.id.apply_button);
        MaterialButton btnClear = filterView.findViewById(R.id.clear_button);

        if (filter_icon != null) {
            filter_icon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        }

        if (close_tab_button != null) {
            close_tab_button.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.START));
        }

        btnApply.setOnClickListener(v -> {
            applyFilters();
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        btnClear.setOnClickListener(v -> {
            clearFilters();
            applyFilters();
        });

        if (history_nav != null) {
            history_nav.setOnClickListener(v -> startActivity(new Intent(this, C5_History_Page.class)));
        }
        
        if (settings_nav != null) {
            settings_nav.setOnClickListener(v -> startActivity(new Intent(this, C13_Settings_Page.class)));
        }

        if (shopping_cart != null) {
            shopping_cart.setOnClickListener(v -> startActivity(new Intent(this, C7_Shopping_Cart.class)));
        }

        if (searchBar != null) {
            searchBar.setOnKeyListener((v, keyCode, event) -> {
                if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN) &&
                        (keyCode == android.view.KeyEvent.KEYCODE_ENTER)) {
                    String query = searchBar.getText().toString();
                    performSearch(query);
                    return true;
                }
                return false;
            });
        }

        // Initialize Menu
        loadMenuItems();

        // Footer color highlight
        if (menu_icon != null && menu_text != null) {
            int activeColor = Color.parseColor("#FFD700");
            menu_icon.setColorFilter(activeColor);
            menu_text.setTextColor(activeColor);
            menu_text.setTypeface(null, Typeface.BOLD);
        }

        ImageView notificationIcon = findViewById(R.id.notification_icon);
        notificationIcon.setOnClickListener(v -> {
            Intent intent = new Intent(C3_Menu_Page.this, NotificationsActivity.class);
            startActivity(intent);
        });

// Optional: fetch unread count and show badge
        DatabaseReference notifRef = FirebaseDatabase.getInstance().getReference("notifications")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        notifRef.orderByChild("read").equalTo(false).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long unreadCount = snapshot.getChildrenCount();
                // update badge on notificationIcon (e.g., show a red circle with number)
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadMenuItems() {
        menuRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fullMenuList.clear();
                foodCardContainer.removeAllViews();
                
                for (DataSnapshot ds : snapshot.getChildren()) {
                    FoodItem item = ds.getValue(FoodItem.class);
                    if (item != null) {
                        item.setId(ds.getKey());
                        if (item.isPublished()) {
                            fullMenuList.add(item);
                            addFoodCard(item);
                        }
                    }
                }
                
                updateNoMenuText();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(C3_Menu_Page.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addFoodCard(FoodItem item) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.customer_item_card_layout, foodCardContainer, false);
        
        TextView name = cardView.findViewById(R.id.item_name);
        TextView price = cardView.findViewById(R.id.item_price);
        ImageView image = cardView.findViewById(R.id.item_image);
        TextView rating = cardView.findViewById(R.id.item_rating);
        TextView quantity = cardView.findViewById(R.id.item_quantity);
        MaterialButton btnAddToCart = cardView.findViewById(R.id.btn_add_to_cart);

        name.setText(item.getName());
        price.setText("RM " + String.format("%.2f", item.getPrice()));
        if (rating != null) rating.setText(String.valueOf(item.getRating()));
        if (quantity != null) quantity.setText("Quantity: " + item.getQuantity());
        
        Glide.with(this).load(item.getImageUrl()).placeholder(R.drawable.no_image_available).into(image);

        cardView.setOnClickListener(v -> {
            Intent intent = new Intent(this, C4_Food_Details.class);
            intent.putExtra("FOOD_ID", item.getId());
            startActivity(intent);
        });

        if (btnAddToCart != null) {
            btnAddToCart.setOnClickListener(v -> {
                CartManager.getInstance().addToCart(item, 1);
                Toast.makeText(this, item.getName() + " added to cart", Toast.LENGTH_SHORT).show();
            });
        }

        foodCardContainer.addView(cardView);
    }

    private void applyFilters() {
        foodCardContainer.removeAllViews();
        List<FoodItem> filteredList = new ArrayList<>();

        for (FoodItem item : fullMenuList) {
            boolean matchesCategory = (!cbMain.isChecked() && !cbSides.isChecked() && !cbDrinks.isChecked()) ||
                    (cbMain.isChecked() && "Main".equalsIgnoreCase(item.getCategory())) ||
                    (cbSides.isChecked() && ("Side".equalsIgnoreCase(item.getCategory()) || "Sides".equalsIgnoreCase(item.getCategory()))) ||
                    (cbDrinks.isChecked() && "Drink".equalsIgnoreCase(item.getCategory()));

            boolean matchesDietary = true;
            if (cbVeg.isChecked() && !item.isVegetarian()) matchesDietary = false;
            if (cbNonVeg.isChecked() && item.isVegetarian()) matchesDietary = false;
            if (cbSpicy.isChecked() && !item.isSpicy()) matchesDietary = false;
            if (cbNonSpicy.isChecked() && item.isSpicy()) matchesDietary = false;

            boolean matchesPrice = true;
            String minStr = etMinPrice.getText().toString();
            String maxStr = etMaxPrice.getText().toString();
            if (!TextUtils.isEmpty(minStr)) {
                if (item.getPrice() < Double.parseDouble(minStr)) matchesPrice = false;
            }
            if (!TextUtils.isEmpty(maxStr)) {
                if (item.getPrice() > Double.parseDouble(maxStr)) matchesPrice = false;
            }

            if (matchesCategory && matchesDietary && matchesPrice) {
                filteredList.add(item);
            }
        }

        if (cbQty.isChecked()) {
            Collections.sort(filteredList, (o1, o2) -> Integer.compare(o2.getQuantity(), o1.getQuantity()));
        } else if (cbRatings.isChecked()) {
            Collections.sort(filteredList, (o1, o2) -> Double.compare(o2.getRating(), o1.getRating()));
        } else if (cbPricing.isChecked()) {
            Collections.sort(filteredList, (o1, o2) -> Double.compare(o1.getPrice(), o2.getPrice()));
        }

        for (FoodItem item : filteredList) {
            addFoodCard(item);
        }
        updateNoMenuText();
    }

    private void clearFilters() {
        cbQty.setChecked(false);
        cbRatings.setChecked(false);
        cbPricing.setChecked(false);
        etMinPrice.setText("");
        etMaxPrice.setText("");
        cbVeg.setChecked(false);
        cbNonVeg.setChecked(false);
        cbSpicy.setChecked(false);
        cbNonSpicy.setChecked(false);
        cbMain.setChecked(false);
        cbSides.setChecked(false);
        cbDrinks.setChecked(false);
    }

    private void performSearch(String query) {
        foodCardContainer.removeAllViews();
        for (FoodItem item : fullMenuList) {
            if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                addFoodCard(item);
            }
        }
        updateNoMenuText();
    }

    private void updateNoMenuText() {
        if (foodCardContainer.getChildCount() == 0) {
            noMenuText.setVisibility(View.VISIBLE);
        } else {
            noMenuText.setVisibility(View.GONE);
        }
    }


}