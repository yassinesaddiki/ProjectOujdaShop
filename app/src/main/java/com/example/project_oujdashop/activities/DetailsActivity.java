package com.example.project_oujdashop.activities;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project_oujdashop.R;
import com.example.project_oujdashop.database.DatabaseHelper;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class DetailsActivity extends AppCompatActivity {

    private ImageView ivProductImage;
    private TextView tvProductName, tvProductPrice, tvCategoryName, tvDescription;
    private ImageView ivQrCode;
    private DatabaseHelper databaseHelper;
    private long productId;
    private String productName, categoryName, description, image;
    private double price;
    private long categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);
        
        // Handle intent data (normal intent or deep link)
        handleIntent(getIntent());
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }
    
    private void handleIntent(Intent intent) {
        // Check if this is a deep link
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri != null && "oujdashop".equals(uri.getScheme()) && "product".equals(uri.getHost())) {
                try {
                    // Extract product ID from path
                    String path = uri.getPath();
                    if (path != null && path.startsWith("/")) {
                        String productIdStr = path.substring(1); // Remove leading slash
                        productId = Long.parseLong(productIdStr);
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }
        } else {
            // Get product ID from normal intent
            productId = intent.getLongExtra("product_id", -1);
        }
        
        // Check if product ID is valid
        if (productId == -1) {
            finish();
            return;
        }
        
        // Get product data
        loadProductData();

        // Initialize views
        initViews();
        
        // Generate QR code
        generateQRCode();
        
        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(productName);
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

    private void loadProductData() {
        Cursor cursor = databaseHelper.getProduct(productId);
        if (cursor.moveToFirst()) {
            productName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
            description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            image = cursor.getString(cursor.getColumnIndexOrThrow("image"));
            categoryId = cursor.getLong(cursor.getColumnIndexOrThrow("category_id"));
            
            // Get category name
            Cursor categoryCursor = databaseHelper.getCategory(categoryId);
            if (categoryCursor.moveToFirst()) {
                categoryName = categoryCursor.getString(categoryCursor.getColumnIndexOrThrow("name"));
            }
            categoryCursor.close();
        }
        cursor.close();
    }

    private void initViews() {
        ivProductImage = findViewById(R.id.ivProductImage);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        tvDescription = findViewById(R.id.tvDescription);
        ivQrCode = findViewById(R.id.ivQrCode);
        
        // Set product data to views
        tvProductName.setText(productName);
        tvProductPrice.setText(String.format("$%.2f", price));
        tvCategoryName.setText(categoryName);
        tvDescription.setText(description);
        
        // Set image if available
        if (image != null && !image.equals("placeholder_image")) {
            try {
                byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ivProductImage.setImageBitmap(decodedBitmap);
            } catch (Exception e) {
                e.printStackTrace();
                ivProductImage.setImageResource(R.drawable.logo);
            }
        } else {
            ivProductImage.setImageResource(R.drawable.logo);
        }
    }

    private void generateQRCode() {
        try {
            // Create QR code content with product ID (using deep link format)
            String qrContent = "oujdashop://product/" + productId;
            
            // Generate QR code
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = multiFormatWriter.encode(qrContent, BarcodeFormat.QR_CODE, 500, 500);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            
            // Set QR code to ImageView
            ivQrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show();
        }
    }
} 