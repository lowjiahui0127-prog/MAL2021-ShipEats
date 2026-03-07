package com.example.shipeatscustomer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class C2_Create_Account extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextInputEditText fullNameEditText, emailEditText, dobEditText, passwordEditText, reenterPasswordEditText;
    private RadioGroup userTypeRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_c2_create_account);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        fullNameEditText = findViewById(R.id.full_name);
        emailEditText = findViewById(R.id.email_address);
        dobEditText = findViewById(R.id.date_of_birth);
        passwordEditText = findViewById(R.id.new_password);
        reenterPasswordEditText = findViewById(R.id.reenter_password);
        userTypeRadioGroup = findViewById(R.id.user_type_radio_group);

        // Pre-select Admin if the Intent came from the Admin Login page
        String userTypeExtra = getIntent().getStringExtra("userType");
        if ("Admin".equals(userTypeExtra)) {
            userTypeRadioGroup.check(R.id.radio_admin);
        }

        findViewById(R.id.create_account_button).setOnClickListener(v -> createAccount());
        findViewById(R.id.login_option).setOnClickListener(v -> finish());
    }

    private void createAccount() {
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String dob = dobEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String reenter = reenterPasswordEditText.getText().toString().trim();

        // Check which radio button is selected
        int selectedId = userTypeRadioGroup.getCheckedRadioButtonId();
        final String userType = (selectedId == R.id.radio_admin) ? "Admin" : "Guest";

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(reenter)) {
            reenterPasswordEditText.setError("Passwords do not match");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String userId = mAuth.getCurrentUser().getUid();

                // Route to "Admins" or "Users" based on selection
                String targetNode = userType.equals("Admin") ? "Admins" : "Users";
                DatabaseReference userRef = mDatabase.child(targetNode).child(userId);

                Map<String, Object> userData = new HashMap<>();
                userData.put("fullName", fullName);
                userData.put("dob", dob);
                userData.put("role", userType);

                userRef.setValue(userData).addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful()) {
                        Toast.makeText(this, userType + " account created!", Toast.LENGTH_SHORT).show();
                        finish(); // Go back to login screen
                    }
                });
            } else {
                Toast.makeText(this, "Registration Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}