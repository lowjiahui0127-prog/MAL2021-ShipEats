package com.example.shipeatscustomer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private MaterialButton admin_option, login_button;
    private TextView create_account;
    private EditText emailInput, passwordInput;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        admin_option = findViewById(R.id.admin_option);
        login_button = findViewById(R.id.login_button);
        create_account = findViewById(R.id.create_account);
        emailInput = findViewById(R.id.email_address);
        passwordInput = findViewById(R.id.password);

        admin_option.setOnClickListener(v -> startActivity(new Intent(this, A1_Login_Page.class)));

        login_button.setOnClickListener(v -> performGuestLogin());

        create_account.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, C2_Create_Account.class);
            intent.putExtra("userType", "Guest"); // Pass the user type
            startActivity(intent);
        });
    }



    private void performGuestLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    String userId = mAuth.getCurrentUser().getUid();
                    mDatabase.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Toast.makeText(MainActivity.this, "Guest Login Successful", Toast.LENGTH_SHORT).show();
                                // FIX: Redirect to C3_Menu_Page instead of A4_CustomerOrderActivity
                                startActivity(new Intent(MainActivity.this, C3_Menu_Page.class));
                                finish();
                            } else {
                                Toast.makeText(MainActivity.this, "Not a registered guest account.", Toast.LENGTH_SHORT).show();
                                mAuth.signOut(); // Sign out as they are not a guest
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Toast.makeText(MainActivity.this, "Database error.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            });
    }
}