package com.example.project_oujdashop.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project_oujdashop.R;
import com.example.project_oujdashop.database.DatabaseHelper;
import com.google.android.material.snackbar.Snackbar;

public class DetailsActivity extends AppCompatActivity {

    private ImageView ivProductImage;
    private TextView tvProductName, tvProductPrice, tvCategoryName, tvDescription;
    private RadioGroup rgSizes;
    private RadioButton rbSizeS, rbSizeM, rbSizeL, rbSizeXL;
    private CheckBox cbFavorite;
    private Switch swNotify;
    private Button btnAddToCart;
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
        
        // Get product ID from intent
        productId = getIntent().getLongExtra("product_id", -1);
        if (productId == -1) {
            finish();
            return;
        }
        
        // Get product data
        loadProductData();

        // Initialize views
        initViews();
        
        // Set click listeners
        setListeners();
        
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
        rgSizes = findViewById(R.id.rgSizes);
        rbSizeS = findViewById(R.id.rbSizeS);
        rbSizeM = findViewById(R.id.rbSizeM);
        rbSizeL = findViewById(R.id.rbSizeL);
        rbSizeXL = findViewById(R.id.rbSizeXL);
        cbFavorite = findViewById(R.id.cbFavorite);
        swNotify = findViewById(R.id.swNotify);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        
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
        
        // Default selection for size
        rbSizeM.setChecked(true);
    }

    private void setListeners() {
        // Checkbox listener
        cbFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Snackbar.make(buttonView, "Added to favorites", Snackbar.LENGTH_SHORT).show();
                cbFavorite.setText(R.string.remove_from_favorites);
            } else {
                Snackbar.make(buttonView, "Removed from favorites", Snackbar.LENGTH_SHORT).show();
                cbFavorite.setText(R.string.add_to_favorites);
            }
        });
        
        // Switch listener
        swNotify.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String message = isChecked ? "You will be notified when this product is on sale" : "Notifications disabled for this product";
            Snackbar.make(buttonView, message, Snackbar.LENGTH_SHORT).show();
        });
        
        // Radio group listener
        rgSizes.setOnCheckedChangeListener((group, checkedId) -> {
            String size = "";
            if (checkedId == R.id.rbSizeS) {
                size = "S";
            } else if (checkedId == R.id.rbSizeM) {
                size = "M";
            } else if (checkedId == R.id.rbSizeL) {
                size = "L";
            } else if (checkedId == R.id.rbSizeXL) {
                size = "XL";
            }
            
            Toast.makeText(this, "Size " + size + " selected", Toast.LENGTH_SHORT).show();
        });
        
        // Add to cart button listener
        btnAddToCart.setOnClickListener(v -> {
            // Get selected size
            String size = "";
            int checkedId = rgSizes.getCheckedRadioButtonId();
            if (checkedId == R.id.rbSizeS) {
                size = "S";
            } else if (checkedId == R.id.rbSizeM) {
                size = "M";
            } else if (checkedId == R.id.rbSizeL) {
                size = "L";
            } else if (checkedId == R.id.rbSizeXL) {
                size = "XL";
            }
            
            // Show success message
            Snackbar.make(v, productName + " (Size " + size + ") added to cart", Snackbar.LENGTH_LONG).show();
        });
    }
} 