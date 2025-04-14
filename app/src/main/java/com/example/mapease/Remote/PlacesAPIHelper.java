package com.example.mapease.Remote;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.mapease.R;
import com.example.mapease.Utils.JsonLogger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PlacesAPIHelper {

    private static final String TAG = "PlacesAPI";
    private static final String PLACES_API_URL = "https://places.googleapis.com/v1/places:searchNearby";

    public static void requestNearbyPlaces(Context context,
                                           double lat,
                                           double lng,
                                           float radius,
                                           String[] includedTypes,
                                           String fieldMask,
                                           int maxResultCount,
                                           com.android.volley.Response.Listener<JSONObject> listener) {

        JSONObject jsonRequest = new JSONObject();

        try {
            // Vị trí trung tâm và bán kính
            JSONObject location = new JSONObject()
                    .put("latitude", lat)
                    .put("longitude", lng);

            JSONObject circle = new JSONObject()
                    .put("center", location)
                    .put("radius", radius);

            JSONObject locationRestriction = new JSONObject()
                    .put("circle", circle);

            // Gộp tất cả vào JSON request
            jsonRequest.put("locationRestriction", locationRestriction);
            jsonRequest.put("maxResultCount", maxResultCount);

            // Mảng includedTypes
            if (includedTypes != null && includedTypes.length > 0) {
                jsonRequest.put("includedTypes", new org.json.JSONArray(includedTypes));
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, PLACES_API_URL, jsonRequest,
                response -> {
                    JsonLogger.logPrettyJson("Response: ", response.toString());

                    listener.onResponse(response);
                },
                error -> Log.e(TAG, "Error: " + error.toString())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Goog-Api-Key", context.getString(R.string.ggMapAPIKey));
                headers.put("X-Goog-FieldMask", fieldMask);
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(jsonObjectRequest);
    }
}
