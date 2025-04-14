package com.example.mapease.Utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

public class LanguageHelper{
    public static Locale getCurrentLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String langCode = prefs.getString("App_Lang", "en");
        return new Locale(langCode);
    }

    public static void saveLocale(Context context, String langCode) {
        SharedPreferences prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        prefs.edit().putString("App_Lang", langCode).apply();
    }
}
