package com.example.mapease;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class yourProfileActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth auth;
    private String userID;

    private ImageView avatar;
    private TextView username, bio;
    private Button writeReviewBtn;
    private ImageButton backToHomeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_your_profile);
        // Find the back to home button (ImageButton)
        backToHomeButton = findViewById(R.id.backToHomeButton);
        writeReviewBtn = findViewById(R.id.writeReview);

        backToHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to go to Home Screen
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        writeReviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(), "Navigating to ReviewSearch", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), ReviewSearch.class));
            }
        });

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://mapease22127072-default-rtdb.asia-southeast1.firebasedatabase.app");
        myRef = database.getReference("user");

        userID = auth.getCurrentUser().getUid();

        avatar = findViewById(R.id.avatar);
        username = findViewById(R.id.username);
        bio = findViewById(R.id.bio);

        loadUserProfile();

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

    private void loadUserProfile() {
        myRef.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("UserID", userID);
                if (snapshot.exists()) {
                    String dbUsername = snapshot.child("username").getValue(String.class);
                    String dbBio = snapshot.child("bio").getValue(String.class);
                    String dbAvatar = snapshot.child("avatar").getValue(String.class);

                    username.setText(dbUsername);
                    bio.setText(dbBio);

                    String avatarUrl = snapshot.child("avatar").getValue(String.class);
                    if (avatarUrl.startsWith("@drawable/")) {
                        // Handle local drawable resources
                        String resourceName = avatarUrl.replace("@drawable/", "");
                        int resId = getResources().getIdentifier(resourceName, "drawable", getPackageName());
                        avatar.setImageResource(resId != 0 ? resId : android.R.drawable.ic_menu_myplaces);
                    } else {
                        // Handle URL images (basic implementation without Glide)
                        new LoadImageTask(avatar).execute(avatarUrl);
                    }
                } else {
                    Toast.makeText(yourProfileActivity.this, "User data not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(yourProfileActivity.this, "Failed to load profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        public LoadImageTask(ImageView imageView) {
            imageViewReference = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                return BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ImageView imageView = imageViewReference.get();
            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else if (imageView != null) {
                imageView.setImageResource(android.R.drawable.ic_menu_myplaces);
            }
        }
    }

}