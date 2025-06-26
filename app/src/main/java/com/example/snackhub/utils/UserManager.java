package com.example.snackhub.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserManager {
    private static UserManager instance;
    private static final String PREFS_NAME = "SnackHubPrefs";
    private static final String KEY_SNACK_ID = "snackId";
    private static final String KEY_SNACK_NAME = "snackName";

    private String snackId;
    private String snackName;
    private Context context;

    private UserManager() {
        // Constructeur privé pour singleton
    }

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context.getApplicationContext();
        loadUserData();
    }

    // Getters
    public String getSnackId() {
        if (snackId == null) {
            loadUserData();
        }
        return snackId;
    }

    public String getSnackName() {
        if (snackName == null) {
            loadUserData();
        }
        return snackName;
    }

    // Setters
    public void setSnackId(String snackId) {
        this.snackId = snackId;
        saveUserData();
    }

    public void setSnackName(String snackName) {
        this.snackName = snackName;
        saveUserData();
    }

    public void setSnackInfo(String snackId, String snackName) {
        this.snackId = snackId;
        this.snackName = snackName;
        saveUserData();
    }

    // Méthodes pour Firebase Auth
    public FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }

    // Sauvegarder dans SharedPreferences
    private void saveUserData() {
        if (context == null) return;

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_SNACK_ID, snackId);
        editor.putString(KEY_SNACK_NAME, snackName);
        editor.apply();
    }

    // Charger depuis SharedPreferences
    private void loadUserData() {
        if (context == null) return;

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        snackId = prefs.getString(KEY_SNACK_ID, null);
        snackName = prefs.getString(KEY_SNACK_NAME, null);
    }

    // Nettoyer les données utilisateur (déconnexion)
    public void clearUserData() {
        snackId = null;
        snackName = null;

        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
        }
    }
}