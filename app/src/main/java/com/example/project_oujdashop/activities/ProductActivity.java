package com.example.project_oujdashop.activities;

import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project_oujdashop.R;
import com.example.project_oujdashop.database.DatabaseHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ProductActivity extends AppCompatActivity {

    private TextView tvCategoryName;
    private GridView gvProducts;
    private FloatingActionButton fabAddProduct;
    private DatabaseHelper databaseHelper;
    private SimpleCursorAdapter adapter;
    private long categoryId;
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);
        
        // Get category ID from intent
        categoryId = getIntent().getLongExtra("category_id", -1);
        if (categoryId == -1) {
            finish();
            return;
        }
        
        // Get category name
        Cursor categoryCursor = databaseHelper.getCategory(categoryId);
        if (categoryCursor.moveToFirst()) {
            categoryName = categoryCursor.getString(categoryCursor.getColumnIndexOrThrow("name"));
        }
        categoryCursor.close();

        // Initialize views
        initViews();
        
        // Load products
        loadProducts();
        
        // Set click listeners
        setListeners();
        
        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(categoryName);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload products when activity resumes
        loadProducts();
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
        tvCategoryName = findViewById(R.id.tvCategoryName);
        gvProducts = findViewById(R.id.gvProducts);
        fabAddProduct = findViewById(R.id.fabAddProduct);
        
        // Set category name
        tvCategoryName.setText(categoryName);
    }

    private void loadProducts() {
        // Get products for this category from database
        Cursor cursor = databaseHelper.getProductsByCategory(categoryId);
        
        // Define which columns to display
        String[] fromColumns = {"name", "price", "image"};
        int[] toViews = {R.id.tvProductName, R.id.tvProductPrice, R.id.ivProductImage};
        
        // Create adapter
        adapter = new SimpleCursorAdapter(
                this,
                R.layout.item_product,
                cursor,
                fromColumns,
                toViews,
                0
        ) {
            @Override
            public void setViewText(TextView v, String text) {
                // Format price if it's the price TextView
                if (v.getId() == R.id.tvProductPrice) {
                    try {
                        double price = Double.parseDouble(text);
                        v.setText(String.format("$%.2f", price));
                    } catch (NumberFormatException e) {
                        v.setText(text);
                    }
                } else {
                    super.setViewText(v, text);
                }
            }
        };
        
        // Set adapter to GridView
        gvProducts.setAdapter(adapter);
    }

    private void setListeners() {
        // Set click listener for FAB
        fabAddProduct.setOnClickListener(v -> {
            // Show dialog to add new product
            showProductDialog(false, -1, null, 0, null, null);
        });
        
        // Set click listener for GridView items
        gvProducts.setOnItemClickListener((parent, view, position, id) -> {
            // Open DetailsActivity with selected product
            Intent intent = new Intent(ProductActivity.this, DetailsActivity.class);
            intent.putExtra("product_id", id);
            startActivity(intent);
        });
        
        // Set long click listener for GridView items
        gvProducts.setOnItemLongClickListener((parent, view, position, id) -> {
            // Get product data
            Cursor cursor = databaseHelper.getProduct(id);
            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                String image = cursor.getString(cursor.getColumnIndexOrThrow("image"));
                
                // Show options dialog
                showProductOptionsDialog(id, name, price, description, image);
            }
            cursor.close();
            return true;
        });
    }

    private void showProductOptionsDialog(long productId, String name, double price, String description, String image) {
        String[] options = {getString(R.string.edit_product), getString(R.string.delete_product)};
        
        new MaterialAlertDialogBuilder(this)
                .setTitle(name)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Edit product
                        showProductDialog(true, productId, name, price, description, image);
                    } else if (which == 1) {
                        // Delete product
                        showDeleteProductDialog(productId, name);
                    }
                })
                .show();
    }

    private void showProductDialog(boolean isEdit, long productId, String name, double price, String description, String image) {
        // Inflate dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_product, null);
        
        // Get views from dialog layout
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextInputEditText etProductName = dialogView.findViewById(R.id.etProductName);
        TextInputEditText etProductPrice = dialogView.findViewById(R.id.etProductPrice);
        TextInputEditText etProductDescription = dialogView.findViewById(R.id.etProductDescription);
        ImageView ivProductImage = dialogView.findViewById(R.id.ivProductImage);
        Button btnSelectImage = dialogView.findViewById(R.id.btnSelectImage);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        TextInputLayout tilProductPrice = dialogView.findViewById(R.id.tilProductPrice);
        
        // Set dialog title based on operation
        tvDialogTitle.setText(isEdit ? R.string.edit_product : R.string.add_product);
        
        // Set existing data if editing
        if (isEdit) {
            etProductName.setText(name);
            etProductPrice.setText(String.valueOf(price));
            etProductDescription.setText(description);
            // Set image if available
            // For simplicity, we're not implementing image selection in this example
        }
        
        // Create dialog
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true)
                .show();
        
        // Set click listeners for dialog buttons
        btnSave.setOnClickListener(v -> {
            String productName = etProductName.getText().toString().trim();
            String productPriceStr = etProductPrice.getText().toString().trim();
            String productDescription = etProductDescription.getText().toString().trim();
            
            if (productName.isEmpty()) {
                etProductName.setError("Product name is required");
                return;
            }
            
            if (productPriceStr.isEmpty()) {
                etProductPrice.setError("Price is required");
                return;
            }
            
            String productPrice = productPriceStr;
            
            // For simplicity, we're using a placeholder for the image
            String productImage = "placeholder_image";
            
            try {
                double priceValue = Double.parseDouble(productPrice);
                
                if (isEdit) {
                    // Update product
                    boolean result = databaseHelper.updateProduct(productId, productName, productDescription, priceValue, productImage, categoryId);
                    if (result) {
                        Snackbar.make(gvProducts, R.string.product_updated, Snackbar.LENGTH_SHORT).show();
                        loadProducts(); // Reload products
                    }
                } else {
                    // Add new product
                    long result = databaseHelper.addProduct(productName, productDescription, priceValue, productImage, categoryId);
                    if (result > 0) {
                        Snackbar.make(gvProducts, R.string.product_added, Snackbar.LENGTH_SHORT).show();
                        loadProducts(); // Reload products
                    }
                }
                dialog.dismiss();
            } catch (NumberFormatException e) {
                tilProductPrice.setError(getString(R.string.error_invalid_price));
            }
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void showDeleteProductDialog(long productId, String name) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_product)
                .setMessage(getString(R.string.confirm_delete_product))
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    boolean result = databaseHelper.deleteProduct(productId);
                    if (result) {
                        Snackbar.make(gvProducts, R.string.product_deleted, Snackbar.LENGTH_SHORT).show();
                        loadProducts(); // Reload products
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
} 