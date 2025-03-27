package com.example.mapease.Remote;
import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.mapease.R;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class RoutesAPIHelper {

    private static final String TAG = "RoutesAPI";
    private static final String ROUTES_API_URL = "https://routes.googleapis.com/directions/v2:computeRoutes";

    public static void requestRoute(Context context, double originLat, double originLng, double destLat, double destLng, Response.Listener<JSONObject> listener) {
        JSONObject jsonRequest = new JSONObject();
        try {
            // Tạo JSON request
            JSONObject origin = new JSONObject();
            origin.put("location", new JSONObject().put("latLng", new JSONObject()
                    .put("latitude", originLat)
                    .put("longitude", originLng)));

            JSONObject destination = new JSONObject();
            destination.put("location", new JSONObject().put("latLng", new JSONObject()
                    .put("latitude", destLat)
                    .put("longitude", destLng)));

            jsonRequest.put("origin", origin);
            jsonRequest.put("destination", destination);
            jsonRequest.put("travelMode", "DRIVE");
            jsonRequest.put("computeAlternativeRoutes", false);
            jsonRequest.put("languageCode", "en-US");
            jsonRequest.put("units", "METRIC");

        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, ROUTES_API_URL, jsonRequest,
                response -> {
                    Log.d(TAG , "Response: " + response.toString());

                    // Gửi response về listener
                    listener.onResponse(response);
                },
                error -> Log.e(TAG , "Error: " + error.toString())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Goog-Api-Key", context.getString(R.string.ggMapAPIKey)); // API Key
                headers.put("X-Goog-FieldMask", "*"); // Nhận toàn bộ dữ liệu (có thể chỉnh nếu cần)
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(jsonObjectRequest);
    }

}

