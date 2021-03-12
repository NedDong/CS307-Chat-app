package com.cs307group9.privatechatchat;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "YourAppName";

    // All Shared Preferences Keys
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_NAME = "name";
    private static final String KEY_PICTURE = "picture";
    private static final String KEY_MOBILE = "mobile";

    //initializing sharedPreferences
    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setMobileNumber(String mobileNumber) {
        editor.putString(KEY_MOBILE, mobileNumber);
        editor.commit();
    }

    public void setName(String name) {
        editor.putString(KEY_NAME, name);
        editor.commit();
    }

    public void setPicture(String picture) {
        editor.putString(KEY_PICTURE, picture);
        editor.commit();
    }

    public String getMobileNumber() {
        return pref.getString(KEY_MOBILE, null);
    }

    //Logging in user and setting the name and profile picture
    public void createLogin(final String mobile) {

        //here, handle the mobile number or email or any details that you
        //use for the login. Then do this:

        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);//false is the default value in case there's nothing found with the key
    }

    public void clearSession() {
        editor.clear();
        editor.commit();
    }
}