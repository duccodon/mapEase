package com.example.mapease;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mapease.model.Review;
import com.example.mapease.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Admin_ReviewDetail extends AppCompatActivity {
    private ImageButton backBtn;
    private TextView userName, content, location, time, id, likeCount;
    private RatingBar ratingBar;
    private String reviewIdStr, reviewerId;

    private FirebaseDatabase database;
    private DatabaseReference reviewRef, userRef;
    private ValueEventListener reviewListener, userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_review_detail);

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance("https://mapease22127072-default-rtdb.asia-southeast1.firebasedatabase.app");

        // Initialize views
        backBtn = findViewById(R.id.backButtonDetailReview);
        userName = findViewById(R.id.adminReviewUser); // Updated to match layout ID
        content = findViewById(R.id.adminReviewContent); // Updated to match layout ID
        location = findViewById(R.id.adminReviewLocation); // Updated to match layout ID
        time = findViewById(R.id.adminReviewTime); // Updated to match layout ID
        likeCount = findViewById(R.id.adminReviewLikeCount); // Updated to match layout ID
        id = findViewById(R.id.adminReviewId); // Updated to match layout ID
        ratingBar = findViewById(R.id.adminReviewRating); // Initialize RatingBar

        // Set up back button
        backBtn.setOnClickListener(v -> finish());

        // Get reviewId from Intent
        Intent intent = getIntent();
        reviewIdStr = intent.getStringExtra("reviewId");
        if (reviewIdStr == null) {
            Toast.makeText(this, "Review ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Reference to specific review
        reviewRef = database.getReference("reviews").child(reviewIdStr);

        // Fetch review data once
        reviewRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        Review review = snapshot.getValue(Review.class);
                        if (review != null) {
                            review.setReviewId(snapshot.getKey()); // Set reviewId from key
                            reviewerId = review.getUserID();

                            // Bind review data to UI
                            content.setText(review.getContent());
                            location.setText(review.getLocationID()); // Resolve to location name if needed
                            time.setText(formatCreateAt(review.getCreateAt().substring(0, 10)));
                            likeCount.setText(String.valueOf(review.getLikes() != null ? review.getLikes().size() - 1 : 0));
                            id.setText("Review ID: " + review.getReviewId());
                            ratingBar.setRating(review.getRating());

                            // Fetch user data
                            fetchUserData();
                        } else {
                            Toast.makeText(Admin_ReviewDetail.this, "Review data is null", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } catch (Exception e) {
                        Log.e("RetrieveReview", "Error parsing review", e);
                        Toast.makeText(Admin_ReviewDetail.this, "Error parsing review data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Admin_ReviewDetail.this, "Review not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Admin_ReviewDetail.this, "Failed to load review: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void fetchUserData() {
        if (reviewerId == null) {
            Toast.makeText(this, "Reviewer ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to specific user
        userRef = database.getReference("user").child(reviewerId);

        // Fetch user data once
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            user.setId(snapshot.getKey());
                            userName.setText(user.getUsername());
                        } else {
                            userName.setText("Unknown User");
                        }
                    } catch (Exception e) {
                        Log.e("RetrieveUser", "Error parsing user", e);
                        userName.setText("Error Loading User");
                    }
                } else {
                    userName.setText("User Not Found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Admin_ReviewDetail.this, "Failed to load user: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                userName.setText("Error Loading User");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listeners to prevent memory leaks
        if (reviewRef != null && reviewListener != null) {
            reviewRef.removeEventListener(reviewListener);
        }
        if (userRef != null && userListener != null) {
            userRef.removeEventListener(userListener);
        }
    }

    // Format createAt timestamp
    private String formatCreateAt(String createAt) {
        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("MMMM d, yyyy");
            java.util.Date date = inputFormat.parse(createAt);
            return "Posted on " + outputFormat.format(date);
        } catch (java.text.ParseException e) {
            return createAt; // Fallback to raw string
        }
    }
}