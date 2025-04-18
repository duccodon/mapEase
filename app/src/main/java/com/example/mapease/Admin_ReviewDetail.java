package com.example.mapease;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapease.model.Review;
import com.example.mapease.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Admin_ReviewDetail extends AppCompatActivity {
    private ImageButton backBtn;
    private TextView userName, content, location, time, id, likeCount;
    private RatingBar ratingBar;
    private RecyclerView imagesRecycler;
    //private Button deleteButton, flagButton;
    private String reviewIdStr, reviewerId;

    private FirebaseDatabase database;
    private DatabaseReference reviewRef, userRef;
    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_review_detail);

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance("https://mapease22127072-default-rtdb.asia-southeast1.firebasedatabase.app");

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.ggMapAPIKey));
        }
        placesClient = Places.createClient(this);

        // Initialize views
        backBtn = findViewById(R.id.backButtonDetailReview);
        userName = findViewById(R.id.adminReviewUser);
        content = findViewById(R.id.adminReviewContent);
        location = findViewById(R.id.adminReviewLocation);
        time = findViewById(R.id.adminReviewTime);
        likeCount = findViewById(R.id.adminReviewLikeCount);
        id = findViewById(R.id.adminReviewId);
        ratingBar = findViewById(R.id.adminReviewRating);
        imagesRecycler = findViewById(R.id.adminReviewImagesRecycler);
        //deleteButton = findViewById(R.id.adminDeleteReviewButton);
        //flagButton = findViewById(R.id.adminFlagReviewButton);

        // Set up back button
        backBtn.setOnClickListener(v -> finish());

        // Set up admin actions
        //deleteButton.setOnClickListener(v -> deleteReview());
        //flagButton.setOnClickListener(v -> flagReview());

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

        // Fetch review data
        reviewRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        Review review = snapshot.getValue(Review.class);
                        if (review != null) {
                            review.setReviewId(snapshot.getKey());
                            reviewerId = review.getUserID();

                            // Bind review data to UI
                            content.setText(review.getContent());
                            time.setText(formatCreateAt(review.getCreateAt()));
                            likeCount.setText(String.valueOf(review.getLikes() != null ? review.getLikes().values().stream().filter(Boolean::booleanValue).count() : 0));
                            id.setText("Review ID: " + review.getReviewId());
                            ratingBar.setRating(review.getRating());

                            // Fetch and bind location
                            fetchLocation(review.getLocationID());

                            // Bind images
                            bindImages(review.getImageUrls());

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

        userRef = database.getReference("user").child(reviewerId);
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

    private void fetchLocation(String locationId) {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.ADDRESS);
        FetchPlaceRequest request = FetchPlaceRequest.builder(locationId, fields).build();

        placesClient.fetchPlace(request).addOnSuccessListener(response -> {
            Place place = response.getPlace();
            String address = place.getAddress() != null ? place.getAddress() : "Unknown Address";
            location.setText(address);
        }).addOnFailureListener(e -> {
            Log.e("Admin_ReviewDetail", "Error fetching place: " + e.getMessage(), e);
            location.setText("Address not available");
        });
    }

    private void bindImages(List<String> base64Images) {
        if (base64Images != null && !base64Images.isEmpty()) {
            imagesRecycler.setVisibility(View.VISIBLE);
            ImageAdapter imageAdapter = new ImageAdapter(this, base64Images);
            imagesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            imagesRecycler.setAdapter(imageAdapter);
        } else {
            imagesRecycler.setVisibility(View.GONE);
        }
    }

    private void deleteReview() {
        reviewRef.removeValue().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Review deleted successfully", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to delete review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void flagReview() {
        // Example: Update a "flagged" field in Firebase
        reviewRef.child("flagged").setValue(true).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Review flagged successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to flag review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // No need to remove listeners since we use addListenerForSingleValueEvent
    }

    private String formatCreateAt(String isoTime) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
            Date date = isoFormat.parse(isoTime);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            return "Posted on " + outputFormat.format(date);
        } catch (ParseException e) {
            Log.e("Admin_ReviewDetail", "Failed to parse date: " + isoTime, e);
            return "Unknown time";
        }
    }

    // Reused ImageAdapter from ReviewAdapter
    private static class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
        private final Context context;
        private final List<String> base64Images;

        public ImageAdapter(Context context, List<String> base64Images) {
            this.context = context;
            this.base64Images = base64Images;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_review_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String base64Image = base64Images.get(position);
            try {
                String pureBase64 = base64Image.startsWith("data:image") ? base64Image.split(",")[1] : base64Image;
                byte[] decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

                options.inSampleSize = calculateInSampleSize(options, 300, 300);
                options.inJustDecodeBounds = false;

                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);
                if (bitmap != null) {
                    holder.imageView.setImageBitmap(bitmap);
                } else {
                    holder.imageView.setImageResource(R.drawable.broken_image);
                }
            } catch (Exception e) {
                Log.e("ImageAdapter", "Error decoding image", e);
                holder.imageView.setImageResource(R.drawable.broken_image);
            }
        }

        private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }
            return inSampleSize;
        }

        @Override
        public int getItemCount() {
            return base64Images != null ? base64Images.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
            }
        }
    }
}