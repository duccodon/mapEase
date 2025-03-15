package com.example.mapease;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class yourProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_your_profile);
        // Find the back to home button (ImageButton)
        ImageButton backToHomeButton = findViewById(R.id.backToHomeButton);
        backToHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to go to Home Screen
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });


        // Sample data for reviews
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review(
                "Center Le Hong Phong FastCare",
                "Mobile phone repair shop",
                "Ho Chi Minh City • 0.9 mi",
                5.0f,
                "6 months ago",
                "Great service!",
                "89 views",
                "Press and hold to react",
                null
        ));
        reviews.add(new Review(
                "Vua Bít Tết Tawaza Lý Thái Tổ",
                "Restaurant • ₫100–200K",
                "Ho Chi Minh City • 0.7 mi",
                5.0f,
                "6 months ago",
                "Food: 5 | Service: 5 | Atmosphere: 5",
                "",
                "",
                1
        ));

        // Set up ListView
        ListView listView = findViewById(R.id.reviewListView);
        ReviewAdapter adapter = new ReviewAdapter(this, reviews);
        listView.setAdapter(adapter);
    }
}