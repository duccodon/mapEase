package com.example.mapease.Utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonLogger {

    public static void logPrettyJson(String tag, String jsonString) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonElement jsonElement = JsonParser.parseString(jsonString);
            String prettyJson = gson.toJson(jsonElement);
            Log.d(tag, "\n" + prettyJson);
        } catch (Exception e) {
            Log.e(tag, "Invalid JSON:\n" + jsonString);
        }
    }
}
