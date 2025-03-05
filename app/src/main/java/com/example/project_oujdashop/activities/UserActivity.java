package com.example.project_oujdashop.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project_oujdashop.R;
import com.example.project_oujdashop.database.DatabaseHelper;
import com.example.project_oujdashop.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class UserActivity extends AppCompatActivity {

    private TextView tvUserEmail;
    private TextInputLayout tilFirstName, tilLastName, tilCurrentPassword, tilNewPassword, tilConfirmPassword;
    private TextInputEditText etFirstName, etLastName, etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnUpdateProfile, btnChangePassword, btnLogout, btnDeleteAccount;
    
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private long userId;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        
        // Initialize database helper and session manager
        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        // Get user ID from session
        userId = sessionManager.getUserId();
        userEmail = sessionManager.getUserEmail();
        
        // Initialize views
        initViews();
        
        // Load user data
        loadUserData();
        
        // Set listeners
        setListeners();
        
        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.profile);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tilFirstName = findViewById(R.id.tilFirstName);
        tilLastName = findViewById(R.id.tilLastName);
        tilCurrentPassword = findViewById(R.id.tilCurrentPassword);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        
        // Set user email
        tvUserEmail.setText(userEmail);
    }

    private void loadUserData() {
        Cursor cursor = databaseHelper.getUser(userId);
        if (cursor.moveToFirst()) {
            String firstName = cursor.getString(cursor.getColumnIndexOrThrow("first_name"));
            String lastName = cursor.getString(cursor.getColumnIndexOrThrow("last_name"));
            
            etFirstName.setText(firstName);
            etLastName.setText(lastName);
        }
        cursor.close();
    }

    private void setListeners() {
        // Update profile button
        btnUpdateProfile.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            
            // Validate inputs
            if (TextUtils.isEmpty(firstName)) {
                tilFirstName.setError("First name is required");
                return;
            }
            
            if (TextUtils.isEmpty(lastName)) {
                tilLastName.setError("Last name is required");
                return;
            }
            
            // Clear errors
            tilFirstName.setError(null);
            tilLastName.setError(null);
            
            // Update user profile
            boolean updated = databaseHelper.updateUserProfile(userId, firstName, lastName);
            if (updated) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Change password button
        btnChangePassword.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            
            // Validate inputs
            if (TextUtils.isEmpty(currentPassword)) {
                tilCurrentPassword.setError("Current password is required");
                return;
            }
            
            if (TextUtils.isEmpty(newPassword)) {
                tilNewPassword.setError("New password is required");
                return;
            }
            
            if (TextUtils.isEmpty(confirmPassword)) {
                tilConfirmPassword.setError("Confirm password is required");
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                tilConfirmPassword.setError("Passwords do not match");
                return;
            }
            
            // Clear errors
            tilCurrentPassword.setError(null);
            tilNewPassword.setError(null);
            tilConfirmPassword.setError(null);
            
            // Verify current password
            boolean verified = databaseHelper.verifyUserPassword(userId, currentPassword);
            if (!verified) {
                tilCurrentPassword.setError("Incorrect password");
                return;
            }
            
            // Update password
            boolean updated = databaseHelper.updateUserPassword(userId, newPassword);
            if (updated) {
                Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                // Clear password fields
                etCurrentPassword.setText("");
                etNewPassword.setText("");
                etConfirmPassword.setText("");
            } else {
                Toast.makeText(this, "Failed to change password", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Logout button
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        sessionManager.logout();
                        startActivity(new Intent(UserActivity.this, LoginActivity.class));
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
        
        // Delete account button
        btnDeleteAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        boolean deleted = databaseHelper.deleteUser(userId);
                        if (deleted) {
                            sessionManager.logout();
                            Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(UserActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
} 