package com.example.project_oujdashop.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "oujdashop.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_CATEGORIES = "categories";
    private static final String TABLE_PRODUCTS = "products";

    // Common column names
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";

    // Users table columns
    public static final String COLUMN_FIRST_NAME = "first_name";
    public static final String COLUMN_LAST_NAME = "last_name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";

    // Categories table columns
    public static final String COLUMN_DESCRIPTION = "description";

    // Products table columns
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_CATEGORY_ID = "category_id";

    // Create table statements
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_FIRST_NAME + " TEXT NOT NULL,"
            + COLUMN_LAST_NAME + " TEXT NOT NULL,"
            + COLUMN_EMAIL + " TEXT NOT NULL UNIQUE,"
            + COLUMN_PASSWORD + " TEXT NOT NULL"
            + ")";

    private static final String CREATE_TABLE_CATEGORIES = "CREATE TABLE " + TABLE_CATEGORIES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NAME + " TEXT NOT NULL,"
            + COLUMN_DESCRIPTION + " TEXT"
            + ")";

    private static final String CREATE_TABLE_PRODUCTS = "CREATE TABLE " + TABLE_PRODUCTS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NAME + " TEXT NOT NULL,"
            + COLUMN_DESCRIPTION + " TEXT,"
            + COLUMN_PRICE + " REAL NOT NULL,"
            + COLUMN_IMAGE + " TEXT,"
            + COLUMN_CATEGORY_ID + " INTEGER NOT NULL,"
            + "FOREIGN KEY(" + COLUMN_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_ID + ") ON DELETE CASCADE"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_CATEGORIES);
        db.execSQL(CREATE_TABLE_PRODUCTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Create tables again
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // User methods
    public long addUser(String firstName, String lastName, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_LAST_NAME, lastName);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);

        // Insert row
        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    public boolean checkUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID},
                COLUMN_EMAIL + "=?", new String[]{email},
                null, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public Cursor loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, new String[]{COLUMN_ID, COLUMN_FIRST_NAME, COLUMN_LAST_NAME, COLUMN_EMAIL},
                COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?", new String[]{email, password},
                null, null, null, null);
    }

    public Cursor getUser(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null,
                COLUMN_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, null, null);
    }

    public boolean updateUserProfile(long userId, String firstName, String lastName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_LAST_NAME, lastName);

        int rowsAffected = db.update(TABLE_USERS, values,
                COLUMN_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
        return rowsAffected > 0;
    }

    public boolean verifyUserPassword(long userId, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID},
                COLUMN_ID + "=? AND " + COLUMN_PASSWORD + "=?", new String[]{String.valueOf(userId), password},
                null, null, null, null);
        boolean verified = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return verified;
    }

    public boolean updateUserPassword(long userId, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, newPassword);

        int rowsAffected = db.update(TABLE_USERS, values,
                COLUMN_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteUser(long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_USERS,
                COLUMN_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
        return rowsAffected > 0;
    }

    // Category methods
    public long addCategory(String name, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_DESCRIPTION, description);

        // Insert row
        long id = db.insert(TABLE_CATEGORIES, null, values);
        db.close();
        return id;
    }

    public Cursor getAllCategories() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CATEGORIES, null,
                null, null,
                null, null, COLUMN_NAME + " ASC");
    }

    public Cursor getCategory(long categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_CATEGORIES, null,
                COLUMN_ID + "=?", new String[]{String.valueOf(categoryId)},
                null, null, null, null);
    }

    public boolean updateCategory(long categoryId, String name, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_DESCRIPTION, description);

        int rowsAffected = db.update(TABLE_CATEGORIES, values,
                COLUMN_ID + "=?", new String[]{String.valueOf(categoryId)});
        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteCategory(long categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_CATEGORIES,
                COLUMN_ID + "=?", new String[]{String.valueOf(categoryId)});
        db.close();
        return rowsAffected > 0;
    }

    // Product methods
    public long addProduct(String name, String description, double price, String image, long categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_IMAGE, image);
        values.put(COLUMN_CATEGORY_ID, categoryId);

        // Insert row
        long id = db.insert(TABLE_PRODUCTS, null, values);
        db.close();
        return id;
    }

    public Cursor getAllProducts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PRODUCTS, null,
                null, null,
                null, null, COLUMN_NAME + " ASC");
    }

    public Cursor getProductsByCategory(long categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PRODUCTS, null,
                COLUMN_CATEGORY_ID + "=?", new String[]{String.valueOf(categoryId)},
                null, null, COLUMN_NAME + " ASC");
    }

    public Cursor getProduct(long productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PRODUCTS, null,
                COLUMN_ID + "=?", new String[]{String.valueOf(productId)},
                null, null, null, null);
    }

    public boolean updateProduct(long productId, String name, String description, double price, String image, long categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_IMAGE, image);
        values.put(COLUMN_CATEGORY_ID, categoryId);

        int rowsAffected = db.update(TABLE_PRODUCTS, values,
                COLUMN_ID + "=?", new String[]{String.valueOf(productId)});
        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteProduct(long productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_PRODUCTS,
                COLUMN_ID + "=?", new String[]{String.valueOf(productId)});
        db.close();
        return rowsAffected > 0;
    }
} 