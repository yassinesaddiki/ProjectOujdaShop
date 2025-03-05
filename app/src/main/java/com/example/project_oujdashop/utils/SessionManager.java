package com.example.project_oujdashop.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SessionManager {
    
    // Shared Preferences
    private SharedPreferences pref;
    
    // Editor for Shared preferences
    private Editor editor;
    
    // Context
    private Context context;
    
    // Shared pref mode
    private int PRIVATE_MODE = 0;
    
    // Sharedpref file name
    private static final String PREF_NAME = "OujdaShopPref";
    
    // All Shared Preferences Keys
    private static final String IS_LOGGED_IN = "IsLoggedIn";
    
    // User id (make variable public to access from outside)
    public static final String KEY_USER_ID = "user_id";
    
    // User email (make variable public to access from outside)
    public static final String KEY_EMAIL = "email";
    
    // User name (make variable public to access from outside)
    public static final String KEY_NAME = "name";
    
    // Constructor
    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }
    
    /**
     * Create login session
     * */
    public void createLoginSession(long userId, String email, String name) {
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGGED_IN, true);
        
        // Storing user id in pref
        editor.putLong(KEY_USER_ID, userId);
        
        // Storing email in pref
        editor.putString(KEY_EMAIL, email);
        
        // Storing name in pref
        editor.putString(KEY_NAME, name);
        
        // commit changes
        editor.commit();
    }
    
    /**
     * Check login method will check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     * */
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGGED_IN, false);
    }
    
    /**
     * Get stored session data
     * */
    public long getUserId() {
        return pref.getLong(KEY_USER_ID, -1);
    }
    
    public String getUserEmail() {
        return pref.getString(KEY_EMAIL, null);
    }
    
    public String getUserName() {
        return pref.getString(KEY_NAME, null);
    }
    
    /**
     * Clear session details
     * */
    public void logout() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
    }
    
    /**
     * Alias for logout method to maintain backward compatibility
     * */
    public void logoutUser() {
        logout();
    }
} 