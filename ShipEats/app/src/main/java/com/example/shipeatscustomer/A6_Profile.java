package com.example.shipeatscustomer;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class A6_Profile extends AppCompatActivity {

    private LinearLayout layoutViewMode, layoutEditMode;
    private FloatingActionButton btnEditAvatar;
    private int startHour = 14;
    private int startMinute = 30;
    private int endHour = 16;
    private int endMinute = 30;

    private TextView tvCurrentHours;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_a6_profile);

        findViewById(R.id.btn_edit_avatar).setOnClickListener(v ->
                Toast.makeText(this, "Change profile picture", Toast.LENGTH_SHORT).show());

        tvCurrentHours = findViewById(R.id.tv_current_hours);

        findViewById(R.id.btn_change_hours).setOnClickListener(v ->
                showChangeHoursDialog()
        );

        findViewById(R.id.card_logout).setOnClickListener(v -> showLogoutDialog());

        setupBottomNav();

        layoutViewMode = findViewById(R.id.layout_view_mode);
        layoutEditMode = findViewById(R.id.layout_edit_mode);
        btnEditAvatar = findViewById(R.id.btn_edit_avatar);

        btnEditAvatar.setVisibility(View.GONE); // hidden in view mode

        findViewById(R.id.btn_edit_profile).setOnClickListener(v -> switchToEditMode());

        findViewById(R.id.btn_cancel).setOnClickListener(v -> switchToViewMode());

        findViewById(R.id.btn_save).setOnClickListener(v -> {
            saveProfileData();
            switchToViewMode();
            showSuccessDialog("Profile updated");
        });
    }

    private void setupBottomNav() {
        findViewById(R.id.dashboard_nav).setOnClickListener(v ->
                startActivity(new Intent(this, A2_Dashboard.class)));

        findViewById(R.id.inventory_nav).setOnClickListener(v ->
                startActivity(new Intent(this, A3_Inventory_Management.class)));
    }

    private void showChangeHoursDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.admin_dialog_change_hours, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        EditText etStartTime = view.findViewById(R.id.et_start_time);
        EditText etEndTime = view.findViewById(R.id.et_end_time);

        // Set previous saved time
        etStartTime.setText(String.format("%02d:%02d", startHour, startMinute));
        etEndTime.setText(String.format("%02d:%02d", endHour, endMinute));

        // Start Time Picker
        etStartTime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(this,
                    (view1, hourOfDay, minute) -> {
                        startHour = hourOfDay;
                        startMinute = minute;
                        etStartTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                    },
                    startHour, startMinute, true); // ← Prefilled here
            timePicker.show();
        });

        // End Time Picker
        etEndTime.setOnClickListener(v -> {
            TimePickerDialog timePicker = new TimePickerDialog(this,
                    (view12, hourOfDay, minute) -> {
                        endHour = hourOfDay;
                        endMinute = minute;
                        etEndTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                    },
                    endHour, endMinute, true); // ← Prefilled here
            timePicker.show();
        });

        view.findViewById(R.id.btn_cancel_hours)
                .setOnClickListener(v -> dialog.dismiss());

        view.findViewById(R.id.btn_save_hours)
                .setOnClickListener(v -> {

                    // Update main profile text
                    String hoursText = "Orders accepted from "
                            + String.format("%02d:%02d", startHour, startMinute)
                            + " to "
                            + String.format("%02d:%02d", endHour, endMinute);

                    tvCurrentHours.setText(hoursText);

                    dialog.dismiss();
                    showSuccessDialog("Ordering hours updated");
                });
    }

    private void showSuccessDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.admin_dialog_success, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        ((TextView) view.findViewById(R.id.tv_success_message)).setText(message);

        view.postDelayed(dialog::dismiss, 1500);
    }



    private void showLogoutDialog() {

        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setIcon(R.drawable.ic_logout)
                .setPositiveButton("Yes", (dialog, which) -> {
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, A1_Login_Page.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void switchToEditMode() {
        layoutViewMode.setVisibility(View.GONE);
        layoutEditMode.setVisibility(View.VISIBLE);
        btnEditAvatar.setVisibility(View.VISIBLE);
    }

    private void switchToViewMode() {
        layoutViewMode.setVisibility(View.VISIBLE);
        layoutEditMode.setVisibility(View.GONE);
        btnEditAvatar.setVisibility(View.GONE);
    }

    private void saveProfileData() {
        EditText etName = findViewById(R.id.et_name);
        EditText etEmail = findViewById(R.id.et_email);
        EditText etPhone = findViewById(R.id.et_phone);
        EditText etLocation = findViewById(R.id.et_location);

        ((TextView) findViewById(R.id.tv_name)).setText(etName.getText().toString());
        ((TextView) findViewById(R.id.tv_email)).setText(etEmail.getText().toString());
        ((TextView) findViewById(R.id.tv_phone)).setText(etPhone.getText().toString());
        ((TextView) findViewById(R.id.tv_location)).setText(etLocation.getText().toString());
    }
}