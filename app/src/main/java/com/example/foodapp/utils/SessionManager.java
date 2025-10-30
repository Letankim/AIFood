package com.example.foodapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.foodapp.models.User;
import com.google.gson.Gson;

public class SessionManager {
    private static final String PREF_NAME = "FoodAppSession";
    private static final String KEY_USER = "current_user";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Gson gson = new Gson();

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER, gson.toJson(user));
        editor.apply();
    }

    public User getUser() {
        String userJson = pref.getString(KEY_USER, null);
        return userJson != null ? gson.fromJson(userJson, User.class) : null;
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}