package com.example.mapease;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
    private EditText bioEdit;
    private String originalBio = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_your_profile);
        // Find the back to home button (ImageButton)
        backToHomeButton = findViewById(R.id.backToHomeButton);
        writeReviewBtn = findViewById(R.id.writeReview);
        editProfileBtn = findViewById(R.id.edit_profile_btn);
        bioEdit = findViewById(R.id.bio_edit);

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

                    originalBio = dbBio != null ? dbBio : "";
                    bioEdit.setText(originalBio);

                    username.setText(dbUsername);
                    bio.setText(dbBio);

                    if (dbAvatar != null && !dbAvatar.isEmpty()) {
                        if (dbAvatar.startsWith("@drawable/")) {
                            // Handle local drawable resource
                            String resourceName = dbAvatar.replace("@drawable/", "");
                            int resId = getResources().getIdentifier(resourceName, "drawable", getPackageName());
                            avatar.setImageResource(resId != 0 ? resId : R.drawable.profile_user);
                        } else {
                            try {
                                // Decode Base64 image
                                byte[] decodedBytes = Base64.decode(dbAvatar, Base64.DEFAULT);
                                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                avatar.setImageBitmap(decodedBitmap);
                            } catch (IllegalArgumentException e) {
                                e.printStackTrace();
                                avatar.setImageResource(R.drawable.profile_user); // fallback
                            }
                        }
                    } else {
                        avatar.setImageResource(R.drawable.profile_user); // fallback
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();

        if (view != null && (ev.getAction() == MotionEvent.ACTION_DOWN)) {
            if (view instanceof EditText) {
                Rect outRect = new Rect();
                view.getGlobalVisibleRect(outRect);

                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    view.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                    String editedBio = bioEdit.getText().toString().trim();
                    if (!editedBio.equals(originalBio)) {
                        showConfirmSaveDialog(editedBio);
                    } else {
                        Toast.makeText(yourProfileActivity.this, "No changes to save", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }


    private void showConfirmSaveDialog(String newBio) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Confirm Bio Change");
        builder.setMessage("Do you want to save this new bio?");

        builder.setPositiveButton("Save", (dialog, which) -> {
            myRef.child(userID).child("bio").setValue(newBio)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Bio updated", Toast.LENGTH_SHORT).show();
                        originalBio = newBio;
                        bio.setText(newBio); // also update the non-edit bio TextView
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update bio", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            bioEdit.setText(originalBio);
            dialog.dismiss();
        });

        builder.setCancelable(false);
        builder.show();
    }

}