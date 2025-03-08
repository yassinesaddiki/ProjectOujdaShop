package com.example.project_oujdashop.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.project_oujdashop.R;
import com.example.project_oujdashop.database.DatabaseHelper;
import com.example.project_oujdashop.utils.CaptureActivityPortrait;
import com.example.project_oujdashop.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {

    private ListView lvCategories;
    private FloatingActionButton fabAddCategory;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private SimpleCursorAdapter adapter;
    
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    
    // Activity result launcher for QR code scanning
    private final ActivityResultLauncher<Intent> barcodeLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String contents = data.getStringExtra("SCAN_RESULT");
                        if (contents != null) {
                            // Parse QR code content
                            // Try to parse as URI
                            try {
                                Uri uri = Uri.parse(contents);
                                if ("oujdashop".equals(uri.getScheme()) && "product".equals(uri.getHost())) {
                                    // Extract product ID from path
                                    String path = uri.getPath();
                                    if (path != null && path.startsWith("/")) {
                                        String productIdStr = path.substring(1); // Remove leading slash
                                        long scannedProductId = Long.parseLong(productIdStr);
                                        
                                        // Check if product exists
                                        Cursor cursor = databaseHelper.getProduct(scannedProductId);
                                        if (cursor.moveToFirst()) {
                                            // Product exists, open details activity
                                            Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                                            intent.putExtra("product_id", scannedProductId);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                                        }
                                        cursor.close();
                                    } else {
                                        Toast.makeText(this, "Invalid QR code format", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(this, "Invalid QR code format", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(this, "Invalid QR code", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);
        
        // Initialize session manager
        sessionManager = new SessionManager(this);
        
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            // User is not logged in, redirect to LoginActivity
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Initialize views
        initViews();
        
        // Load categories
        loadCategories();
        
        // Set click listeners
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload categories when activity resumes
        loadCategories();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_profile) {
            // Open UserActivity
            startActivity(new Intent(MainActivity.this, UserActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            // Logout user
            sessionManager.logoutUser();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        } else if (id == R.id.action_scan_qr) {
            // Start QR code scanner
            if (checkCameraPermission()) {
                startQRCodeScanner();
            } else {
                requestCameraPermission();
            }
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        lvCategories = findViewById(R.id.lvCategories);
        fabAddCategory = findViewById(R.id.fabAddCategory);
    }

    private void loadCategories() {
        Cursor cursor = databaseHelper.getAllCategories();
        
        String[] fromColumns = {
                "_id",
                "name",
                "description"
        };
        
        int[] toViews = {
                R.id.tvCategoryId, // This is hidden
                R.id.tvCategoryName,
                R.id.tvCategoryDescription // This might need to be added to the layout
        };
        
        adapter = new SimpleCursorAdapter(
                this,
                R.layout.item_category,
                cursor,
                fromColumns,
                toViews,
                0
        );
        
        lvCategories.setAdapter(adapter);
    }

    private void setListeners() {
        // Add category button
        fabAddCategory.setOnClickListener(v -> {
            showCategoryDialog(false, -1, "", "");
        });
        
        // Category item click
        lvCategories.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(MainActivity.this, ProductActivity.class);
            intent.putExtra("category_id", id);
            startActivity(intent);
        });
        
        // Category item long click
        lvCategories.setOnItemLongClickListener((parent, view, position, id) -> {
            Cursor cursor = (Cursor) adapter.getItem(position);
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            
            showCategoryOptionsDialog(id, name, description);
            return true;
        });
    }

    private void showCategoryOptionsDialog(long categoryId, String name, String description) {
        String[] options = {getString(R.string.edit_category), getString(R.string.delete_category)};
        
        new AlertDialog.Builder(this)
                .setTitle(name)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Edit category
                        showCategoryDialog(true, categoryId, name, description);
                    } else {
                        // Delete category
                        showDeleteCategoryDialog(categoryId, name);
                    }
                })
                .show();
    }

    private void showCategoryDialog(boolean isEdit, long categoryId, String name, String description) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_category, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        // Get views
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextInputLayout tilCategoryName = dialogView.findViewById(R.id.tilCategoryName);
        TextInputEditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        TextInputLayout tilCategoryDescription = dialogView.findViewById(R.id.tilCategoryDescription);
        TextInputEditText etCategoryDescription = dialogView.findViewById(R.id.etCategoryDescription);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        
        // Set dialog title
        tvDialogTitle.setText(isEdit ? R.string.edit_category : R.string.add_category);
        
        // Set existing data if editing
        if (isEdit) {
            etCategoryName.setText(name);
            etCategoryDescription.setText(description);
        }
        
        // Create dialog
        AlertDialog dialog = builder.setView(dialogView).create();
        
        // Save button click
        btnSave.setOnClickListener(v -> {
            String categoryName = etCategoryName.getText().toString().trim();
            String categoryDescription = etCategoryDescription.getText().toString().trim();
            
            // Validate input
            if (categoryName.isEmpty()) {
                tilCategoryName.setError(getString(R.string.error_empty_field));
                return;
            }
            
            // Clear errors
            tilCategoryName.setError(null);
            
            // Save category
            boolean success;
            if (isEdit) {
                success = databaseHelper.updateCategory(categoryId, categoryName, categoryDescription);
                if (success) {
                    Toast.makeText(MainActivity.this, R.string.success_update, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, R.string.error_update_failed, Toast.LENGTH_SHORT).show();
                }
            } else {
                long result = databaseHelper.addCategory(categoryName, categoryDescription);
                success = result != -1;
                if (success) {
                    Toast.makeText(MainActivity.this, R.string.success_update, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, R.string.error_update_failed, Toast.LENGTH_SHORT).show();
                }
            }
            
            if (success) {
                // Refresh categories
                loadCategories();
                dialog.dismiss();
            }
        });
        
        // Cancel button click
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    private void showDeleteCategoryDialog(long categoryId, String name) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_category)
                .setMessage(getString(R.string.confirm_delete_category))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    // Delete category
                    databaseHelper.deleteCategory(categoryId);
                    Snackbar.make(lvCategories, R.string.category_deleted, Snackbar.LENGTH_SHORT).show();
                    loadCategories(); // Reload categories
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQRCodeScanner();
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void startQRCodeScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan a product QR code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(false);
        options.setCaptureActivity(CaptureActivityPortrait.class);
        
        // Launch the scanning activity
        Intent intent = options.createScanIntent(this);
        barcodeLauncher.launch(intent);
    }
} 