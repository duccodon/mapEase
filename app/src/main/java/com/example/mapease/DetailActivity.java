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

public class DetailActivity extends AppCompatActivity {
    ImageView img;
    TextView email, bio, username, role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);

        email = findViewById(R.id.detailEmail);
        bio = findViewById(R.id.detailBio);
        username = findViewById(R.id.detailUsername);
        role = findViewById(R.id.detailRole);

        Intent intent = getIntent();
        email.setText(intent.getStringExtra("email"));
        role.setText(intent.getStringExtra("role"));
        bio.setText(intent.getStringExtra("bio"));
        username.setText(intent.getStringExtra("username"));

    }
}