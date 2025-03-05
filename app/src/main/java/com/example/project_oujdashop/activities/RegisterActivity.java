package com.example.project_oujdashop.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_oujdashop.R;
import com.example.project_oujdashop.database.DatabaseHelper;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilFirstName, tilLastName, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etFirstName, etLastName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        initViews();
        
        // Set click listeners
        setListeners();
    }

    private void initViews() {
        tilFirstName = findViewById(R.id.tilFirstName);
        tilLastName = findViewById(R.id.tilLastName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void setListeners() {
        btnRegister.setOnClickListener(v -> {
            if (validateInputs()) {
                String firstName = etFirstName.getText().toString().trim();
                String lastName = etLastName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                
                // Add user to database
                long id = databaseHelper.addUser(firstName, lastName, email, password);
                
                if (id > 0) {
                    // Registration successful
                    Snackbar.make(v, R.string.registration_success, Snackbar.LENGTH_LONG).show();
                    
                    // Clear all input fields
                    clearInputs();
                    
                    // Finish activity after a delay
                    v.postDelayed(this::finish, 2000);
                } else {
                    // Registration failed
                    Snackbar.make(v, R.string.error_message, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate first name
        if (TextUtils.isEmpty(etFirstName.getText())) {
            tilFirstName.setError("First name is required");
            isValid = false;
        } else {
            tilFirstName.setError(null);
        }

        // Validate last name
        if (TextUtils.isEmpty(etLastName.getText())) {
            tilLastName.setError("Last name is required");
            isValid = false;
        } else {
            tilLastName.setError(null);
        }

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
        } else if (etPassword.getText().toString().length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        // Validate confirm password
        if (TextUtils.isEmpty(etConfirmPassword.getText())) {
            tilConfirmPassword.setError("Confirm password is required");
            isValid = false;
        } else if (!etConfirmPassword.getText().toString().equals(etPassword.getText().toString())) {
            tilConfirmPassword.setError(getString(R.string.passwords_not_match));
            isValid = false;
        } else {
            tilConfirmPassword.setError(null);
        }

        return isValid;
    }

    private void clearInputs() {
        etFirstName.setText("");
        etLastName.setText("");
        etEmail.setText("");
        etPassword.setText("");
        etConfirmPassword.setText("");
    }
} 