package com.example.mapease;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mapease.model.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WriteReview extends AppCompatActivity {
    private TextView placeName, userName;
    private LinearLayout starRating;
    private EditText reviewInput;
    private Button postButton;
    private Button btnAddPhoto;
    private LinearLayout imageContainer;

    private int selectedRating = 0;
    private List<Uri> selectedImageUris = new ArrayList<>();

    // Firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase, myRef;//myRef

    private static final int PICK_IMAGE_REQUEST = 1; // Request code for selecting an image
    private static final int PERMISSION_REQUEST_CODE = 2; // Permission request code
    String context;
    private String locationId, userId;
    private String locationName;

    // For image selection
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUris.add(uri);
                    addImageToContainer(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_write_review);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://mapease22127072-default-rtdb.asia-southeast1.firebasedatabase.app");
        mDatabase = database.getReference("reviews");
        myRef = database.getReference("user");

        // Initialize views
        placeName = findViewById(R.id.place_name);
        starRating = findViewById(R.id.star_rating);
        reviewInput = findViewById(R.id.review_input);
        postButton = findViewById(R.id.post_button);
        btnAddPhoto = findViewById(R.id.add_photos_button);
        imageContainer = findViewById(R.id.image_container);
        userName = findViewById(R.id.user_name);

        // Get intent data
        Intent intent = getIntent();
        locationName = intent.getStringExtra("placeName");
        locationId = intent.getStringExtra("locationID");
        userId = intent.getStringExtra("userId");
        context = intent.getStringExtra("context");
        placeName.setText(locationName);

        myRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("UserID", userId);
                if (snapshot.exists()) {
                    userName.setText(snapshot.child("username").getValue(String.class));
                } else {
                    Toast.makeText(getApplicationContext(), "User data not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Failed to load profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Post button click
        postButton.setOnClickListener(v -> postReview());

        btnAddPhoto.setOnClickListener(v -> {
            if (selectedImageUris.size() < 5) { // Limit to 5 images
                galleryLauncher.launch("image/*");
            } else {
                Toast.makeText(this, "Maximum 5 images allowed", Toast.LENGTH_SHORT).show();
            }
        });

        //return button
        ImageButton returnHomeButton = findViewById(R.id.backToHomeButton);
        returnHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context.contentEquals("main"))
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                else if (context.contentEquals("profile")) {
                    startActivity(new Intent(getApplicationContext(), yourProfileActivity.class));
                }
                finish();
            }
        });

        // Optionally, you can implement the star rating system and set the stars dynamically
        setupStarRating();
    }

    private void setupStarRating() {
        for (int i = 0; i < starRating.getChildCount(); i++) {
            ImageButton star = (ImageButton) starRating.getChildAt(i);
            final int index = i;
            star.setOnClickListener(v -> updateStars(index));
        }
    }

    private void updateStars(int index) {
        selectedRating = index + 1;
        for (int i = 0; i < starRating.getChildCount(); i++) {
            ImageButton star = (ImageButton) starRating.getChildAt(i);
            star.setImageResource(i <= index ? R.drawable.ic_star_filled : R.drawable.ic_star_empty);
        }
    }

    private void addImageToContainer(Uri imageUri) {
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageURI(imageUri);

        // Add remove button
        ImageButton removeButton = new ImageButton(this);
        removeButton.setImageResource(R.drawable.ic_close);
        removeButton.setBackground(null);
        removeButton.setOnClickListener(v -> {
            imageContainer.removeView(imageView);
            imageContainer.removeView(removeButton);
            selectedImageUris.remove(imageUri);
        });

        imageContainer.addView(imageView);
        imageContainer.addView(removeButton);
    }

    private void postReview() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please sign in to post a review", Toast.LENGTH_SHORT).show();
            return;
        }

        String reviewText = reviewInput.getText().toString().trim();
        if (reviewText.isEmpty()) {
            Toast.makeText(this, "Please enter your review", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedRating == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        postButton.setEnabled(false);

        Log.d("WriteReview", "Location ID: " + locationId);
        if (locationId == null || locationId.isEmpty()) {
            Toast.makeText(this, "Invalid location", Toast.LENGTH_SHORT).show();
            Log.e("WriteReview", "Location ID is null or empty");
            return;
        }

        if (user.getUid() == null || user.getUid().isEmpty()) {
            Toast.makeText(this, "Invalid user information", Toast.LENGTH_SHORT).show();
            Log.e("WriteReview", "User ID is null or empty");
            return;
        }

        String reviewId = user.getUid() + "_" + locationId;
        String userId = user.getUid();

        if (selectedImageUris.isEmpty()) {
            saveReviewToDatabase(reviewId, userId, new ArrayList<>());
        } else {
            // Convert images to Base64
            List<String> base64Images = new ArrayList<>();
            for (Uri imageUri : selectedImageUris) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // 70% quality to reduce size
                    byte[] imageBytes = baos.toByteArray();
                    String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                    base64Images.add(base64Image);
                } catch (Exception e) {
                    Log.e("ImageConversion", "Error converting image to Base64", e);
                }
            }
            saveReviewToDatabase(reviewId, userId, base64Images);
        }
    }

    private void saveReviewToDatabase(String reviewId, String userId, List<String> base64Images) {
        String reviewText = reviewInput.getText().toString().trim();
        String createdAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()).format(new Date());

        Map<String, Boolean> likes = new HashMap<>();
        likes.put("dummy", false);

        Review review = new Review(
                reviewId,
                userId,
                locationId,
                reviewText,
                selectedRating,
                createdAt,
                base64Images, // Now storing Base64 strings instead of URLs
                likes
        );

        mDatabase.child(reviewId).setValue(review)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(WriteReview.this, "Review posted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(WriteReview.this, "Failed to post review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    postButton.setEnabled(true);
                });
    }

    /*private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            // Convert URI to Bitmap and display it
            try {
                String[] projection = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImageUri, projection, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(projection[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();
                    Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                    imageView.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }*/
}