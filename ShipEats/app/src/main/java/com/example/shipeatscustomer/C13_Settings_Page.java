package com.example.shipeatscustomer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;

public class C13_Settings_Page extends AppCompatActivity {

    // UI Elements for the Student ID Card
    private TextView tvCardName, tvCardID, tvCardValidity;
    private ImageView ivCardProfilePicture;

    // Layout and Navigation
    private DrawerLayout drawerLayout;
    private ActivityResultLauncher<Intent> profileResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_c13_settings_page);

        // 1. Initialize Card Views
        tvCardName = findViewById(R.id.tvStudentName);
        tvCardID = findViewById(R.id.tvStudentID);
        tvCardValidity = findViewById(R.id.tvValidity);
        ivCardProfilePicture = findViewById(R.id.ivProfilePicture);

        // 2. Initialize Layout and Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        ImageView closeTabBtn = findViewById(R.id.close_tab_button); // Inside drawer

        // 3. Initialize Footer Navigation (Inside include tag)
        LinearLayout history_nav = findViewById(R.id.history_nav);
        LinearLayout settings_nav = findViewById(R.id.settings_nav);
        LinearLayout menu_nav = findViewById(R.id.menu_nav);

        // 4. Setup Activity Result Launcher (Refreshes ID card after editing profile)
        profileResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // FIX: Handled deprecated getSerializableExtra for modern Android versions
                        Student updatedStudent;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            updatedStudent = result.getData().getSerializableExtra("STUDENT_DATA", Student.class);
                        } else {
                            updatedStudent = (Student) result.getData().getSerializableExtra("STUDENT_DATA");
                        }
                        
                        if (updatedStudent != null) {
                            updateCardUI(updatedStudent);
                        }
                    }
                }
        );

        // 5. Drawer & Footer Click Listeners
        if (closeTabBtn != null) {
            closeTabBtn.setOnClickListener(v -> {
                if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
            });
        }

        if (history_nav != null) history_nav.setOnClickListener(v -> startActivity(new Intent(this, C5_History_Page.class)));
        if (menu_nav != null) menu_nav.setOnClickListener(v -> startActivity(new Intent(this, C3_Menu_Page.class)));

        // We are already on Settings, so just close the drawer
        if (settings_nav != null) {
            settings_nav.setOnClickListener(v -> {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            });
        }

        // 6. Load Initial Data and Setup Menu
        loadSavedProfile();
        setupMenuNavigation();
    }

    //Sets up click listeners for the individual settings rows in the ScrollView
    private void setupMenuNavigation() {
        // General Section
        View btnEditProfile = findViewById(R.id.btnEditProfile);
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(this, C12_Profile.class);
                profileResultLauncher.launch(intent);
            });
        }

        View btnOrderHistory = findViewById(R.id.btnOrderHistory);
        if (btnOrderHistory != null) {
            btnOrderHistory.setOnClickListener(v ->
                    startActivity(new Intent(this, C5_History_Page.class)));
        }

        View btnPaymentMethods = findViewById(R.id.btnPaymentMethods);
        if (btnPaymentMethods != null) {
            btnPaymentMethods.setOnClickListener(v ->
                    startActivity(new Intent(this, C11_Payment_Method.class)));
        }

        View btnNotifications = findViewById(R.id.btnNotifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v ->
                    startActivity(new Intent(this, C10_Notifications.class)));
        }

        // About Section
        View btnAbout = findViewById(R.id.btnAboutShipEats);
        if (btnAbout != null) {
            btnAbout.setOnClickListener(v ->
                    startActivity(new Intent(this, C9_About.class)));
        }

        View btnTerms = findViewById(R.id.btnTerms);
        if (btnTerms != null) {
            btnTerms.setOnClickListener(v ->
                    startActivity(new Intent(this, C14_Terms_And_Condition.class)));
        }

        // Logout
        View btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showLogoutDialog());
        }
    }

    
      // Loads the profile from SharedPreferences when the activity starts
    private void loadSavedProfile() {
        SharedPreferences sharedPreferences = getSharedPreferences("StudentPrefs", MODE_PRIVATE);

        String name = sharedPreferences.getString("name", "Hoo Soline");
        String id = sharedPreferences.getString("studentID", "050505070505");
        String imageUriString = sharedPreferences.getString("imageUri", "");

        if (tvCardName != null) tvCardName.setText(name);
        if (tvCardID != null) tvCardID.setText(id);

        if (!imageUriString.isEmpty() && ivCardProfilePicture != null) {
            Glide.with(this)
                    .load(Uri.parse(imageUriString))
                    .placeholder(R.drawable.placeholder_user)
                    .circleCrop()
                    .into(ivCardProfilePicture);
        }
    }

    //Updates the UI immediately when data is returned from the Profile screen
    private void updateCardUI(Student student) {
        if (tvCardName != null) tvCardName.setText(student.getName());

        if (student.getProfileImageUri() != null && !student.getProfileImageUri().isEmpty() && ivCardProfilePicture != null) {
            Glide.with(this)
                    .load(Uri.parse(student.getProfileImageUri()))
                    .placeholder(R.drawable.placeholder_user)
                    .circleCrop()
                    .into(ivCardProfilePicture);
        }
    }

    //Shows a confirmation dialog before logging out and clearing the task stack
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(C13_Settings_Page.this, MainActivity.class);
                    // Clear the backstack so the user cannot press back to return to settings
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
