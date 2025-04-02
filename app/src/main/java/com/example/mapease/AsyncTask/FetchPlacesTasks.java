package com.example.mapease.AsyncTask;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchPlacesTasks extends AsyncTask<String, Void, String> {
    private GoogleMap mMap;

    public FetchPlacesTasks(GoogleMap mMap) {
        this.mMap = mMap;
    }

    @Override
    protected String doInBackground(String... urls) {
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(urls[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
        } catch (Exception e) {
            Log.e("FetchPlacesTask", "Error fetching data", e);
        }
        return result.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray results = jsonObject.getJSONArray("results");

            mMap.clear();
            for (int i = 0; i < results.length(); i++) {
                JSONObject place = results.getJSONObject(i);
                JSONObject location = place.getJSONObject("geometry").getJSONObject("location");

                LatLng latLng = new LatLng(location.getDouble("lat"), location.getDouble("lng"));
                String name = place.getString("name");

                mMap.addMarker(new MarkerOptions().position(latLng).title(name));
            }
        } catch (Exception e) {
            Log.e("FetchPlacesTask", "Error parsing data", e);
        }
    }

}
