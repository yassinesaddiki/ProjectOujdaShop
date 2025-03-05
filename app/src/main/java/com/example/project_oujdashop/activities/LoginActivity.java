package com.example.project_oujdashop.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_oujdashop.R;
import com.example.project_oujdashop.database.DatabaseHelper;
import com.example.project_oujdashop.utils.SessionManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin, btnRegister;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);
        
        // Initialize session manager
        sessionManager = new SessionManager(this);
        
        // If user is already logged in, redirect to MainActivity
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        // Initialize views
        initViews();
        
        // Set click listeners
        setListeners();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void setListeners() {
        btnLogin.setOnClickListener(v -> {
            if (validateInputs()) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                
                // Check login credentials
                Cursor cursor = databaseHelper.loginUser(email, password);
                if (cursor != null && cursor.moveToFirst()) {
                    // Get user data
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                    String firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FIRST_NAME));
                    String lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_NAME));
                    
                    // Create login session with name as firstName + lastName
                    String name = firstName + " " + lastName;
                    sessionManager.createLoginSession(id, email, name);
                    
                    // Redirect to MainActivity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    
                    cursor.close();
                } else {
                    // Show error message
                    Snackbar.make(v, R.string.login_failed, Snackbar.LENGTH_LONG).show();
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        });

        btnRegister.setOnClickListener(v -> {
            // Redirect to RegisterActivity
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate email
        if (TextUtils.isEmpty(etEmail.getText())) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        // Validate password
        if (TextUtils.isEmpty(etPassword.getText())) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        return isValid;
    }
} 