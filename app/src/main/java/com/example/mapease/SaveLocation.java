package com.example.mapease;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mapease.model.favoriteLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SaveLocation extends AppCompatActivity {

    // Firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;//myRef

    String context;

    private String locationId;

    private String favoriteId;

    private String userId;

    private String locationName;

    private String locationAddress;

    private String locationNotes = "";

    private String locationType = "";

    private LatLng locationLatLng;

    private String imageUrls;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://mapease22127072-default-rtdb.asia-southeast1.firebasedatabase.app");
        mDatabase = database.getReference("favoriteLocations");

        // Get the user ID
        userId = auth.getCurrentUser().getUid();
        // Initialize the image URLs list


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            locationName = extras.getString("selectedName");
            locationLatLng = extras.getParcelable("selectedLatLng");
            locationAddress = extras.getString("selectedAddress");
            locationType = extras.getString("selectedPlaceType");
            locationId = extras.getString("selectedPlaceID");
            context = extras.getString("context");
            imageUrls = extras.getString("placeImageBase64");
        }

        if (context != null && context.equals("remove")) {
            // Remove the location from favorites
            removeFavoriteLocationFromDB(userId + "_" + locationId);
        } else {
            saveFavoriteLocationToDB(locationId, locationName, locationAddress, locationNotes, locationType, locationLatLng, imageUrls);
        }
        finish();
    }

    private  void saveFavoriteLocationToDB (String locationId, String locationName, String locationAddress, String locationNotes, String locationType, LatLng locationLatLng, String imageUrls) {
        // Create a new FavoriteLocation object
        favoriteId = userId + "_" + locationId;
        String createdAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()).format(new Date());


        favoriteLocation favoriteLocation = new favoriteLocation(favoriteId, userId, locationId, locationLatLng ,locationName, locationAddress, locationNotes, locationType, createdAt, imageUrls);

        // Save the favorite location to the database
        mDatabase.child(favoriteId).setValue(favoriteLocation)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SaveLocation.this, "Save location successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SaveLocation.this, "Save location successfully failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private  void removeFavoriteLocationFromDB (String favoriteId) {

        mDatabase.child(favoriteId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SaveLocation.this, "Remove location successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SaveLocation.this, "Remove location failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
