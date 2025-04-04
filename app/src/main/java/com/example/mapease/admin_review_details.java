package com.example.mapease;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class admin_review_details extends AppCompatActivity {
    ImageView img;
    TextView location, content, rating, createdAt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_review_details);

        location = findViewById(R.id.detailLocation);
        content = findViewById(R.id.detailContent);
        rating = findViewById(R.id.detailRating);
        createdAt = findViewById(R.id.detailCreatedAt);

        Intent intent = getIntent();
        location.setText(intent.getStringExtra("location"));
        rating.setText(intent.getStringExtra("rating"));
        createdAt.setText(intent.getStringExtra("createdAt"));
        content.setText(intent.getStringExtra("content"));
    }
}