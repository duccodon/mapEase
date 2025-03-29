package com.example.mapease;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.Arrays;

public class ReviewSearch extends AppCompatActivity {
    private AutocompleteSupportFragment autocompleteSupportFragment;
    Button btnSearch;
    private View searchView;
    ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_review_search);
        btnBack = findViewById(R.id.backToProfileButton);

        autocompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager()
                .findFragmentById(R.id.search_review);
        assert autocompleteSupportFragment != null;
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.FORMATTED_ADDRESS, Place.Field.DISPLAY_NAME, Place.Field.LAT_LNG));

        autocompleteSupportFragment.setHint(getString(R.string.search_review));

        if (autocompleteSupportFragment != null) {
            searchView = autocompleteSupportFragment.getView();
            if (searchView != null) {
                searchView.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_search_view));
            }
        }

        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                if (place.getLocation() != null) {

                    // Update instance variables
                    String placeName = place.getName();
                    String placeLatitude = String.valueOf(place.getLatLng().latitude);
                    String placeLongitude = String.valueOf(place.getLatLng().longitude);
                    String placeAddress = place.getAddress();

                    Intent i = new Intent(getApplicationContext(), WriteReview.class);
                    i.putExtra("placeName", placeName);
                    i.putExtra("placeAddress", placeAddress);
                    i.putExtra("placeLongitude", placeLongitude);
                    i.putExtra("placeLatitude", placeLatitude);
                    startActivity(i);

                } else {
                    Snackbar.make(findViewById(android.R.id.content),
                            "No coordinates found for this place!",
                            Snackbar.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onError(@NonNull Status status) {
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), yourProfileActivity.class));
            }
        });

    }
}