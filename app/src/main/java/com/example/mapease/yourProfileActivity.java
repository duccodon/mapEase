package com.example.mapease;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
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

import com.example.mapease.adapter.ReviewAdapter;
import com.example.mapease.model.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class yourProfileActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private DatabaseReference reviewRef;
    private FirebaseAuth auth;
    private String userID;

    private ImageView avatar;
    private TextView username, bio, contribution;
    private Button writeReviewBtn, editProfileBtn;
    private ImageButton backToHomeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_your_profile);
        // Find the back to home button (ImageButton)
        backToHomeButton = findViewById(R.id.backToHomeButton);
        writeReviewBtn = findViewById(R.id.writeReview);
        editProfileBtn = findViewById(R.id.edit_profile_btn);

        backToHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to go to Home Screen
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), EditProfile.class));
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
        reviewRef = database.getReference("reviews");

        userID = auth.getCurrentUser().getUid();

        avatar = findViewById(R.id.avatar);
        username = findViewById(R.id.username);
        bio = findViewById(R.id.bio);
        contribution = findViewById(R.id.reviews_count);

        loadUserProfile();
        loadAllReviews();
    }

    private void loadAllReviews() {
        reviewRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Review> reviews = new ArrayList<>();

                for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                    try {
                        Review review = reviewSnapshot.getValue(Review.class);
                        if (review != null && review.getUserID().contentEquals(userID))
                            reviews.add(review);
                    } catch (Exception e) {
                        Log.e("RetrieveReview", "Error parsing review", e);
                    }
                }

                for (Review review : reviews)
                    Log.d("RetrieveReview", review.toString());
                contribution.setText(String.valueOf(reviews.size()));

                // Update adapter with real reviews
                ReviewAdapter adapter = new ReviewAdapter(yourProfileActivity.this, reviews);
                ListView listView = findViewById(R.id.reviewListView);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(yourProfileActivity.this,
                        "Failed to load reviews: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
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