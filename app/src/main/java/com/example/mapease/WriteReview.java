package com.example.mapease;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WriteReview extends AppCompatActivity {
    private TextView placeName;
    private LinearLayout starRating;
    private EditText reviewInput;
    private TextView addPhotosButton;
    private Button postButton;
    private Button btnAddPhoto;
    private ImageView imageView;
    private static final int PICK_IMAGE_REQUEST = 1; // Request code for selecting an image
    private static final int PERMISSION_REQUEST_CODE = 2; // Permission request code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_write_review);

        // Initialize views
        placeName = findViewById(R.id.place_name);
        starRating = findViewById(R.id.star_rating);
        reviewInput = findViewById(R.id.review_input);
        postButton = findViewById(R.id.post_button);
        btnAddPhoto = findViewById(R.id.add_photos_button);
        Intent intent = getIntent();
        placeName.setText(intent.getStringExtra("placeName"));

        imageView = findViewById(R.id.image_view);

        // Handle Post button click
        postButton.setOnClickListener(v -> {
            // Here, you can add the functionality to post the review, maybe send the data to a backend
            String reviewText = reviewInput.getText().toString();
            // Handle the review post (e.g., save to database or send to server)
        });

        btnAddPhoto.setOnClickListener(v -> {
            // Check permission
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                openGallery();
            }
        });

        // Optionally, you can implement the star rating system and set the stars dynamically
        setupStarRating();
    }

    private void setupStarRating() {
        // This is a simple example of how you might handle the star rating
        // You can replace this with real interaction like click to fill the stars
        for (int i = 0; i < starRating.getChildCount(); i++) {
            ImageButton star = (ImageButton) starRating.getChildAt(i);
            final int index = i;
            star.setOnClickListener(v -> updateStars(index));
        }
    }

    private void updateStars(int index) {
        // Update the stars based on the index clicked
        for (int i = 0; i < starRating.getChildCount(); i++) {
            ImageButton star = (ImageButton) starRating.getChildAt(i);
            if (i <= index) {
                star.setImageResource(R.drawable.ic_star_filled); // Set filled star
            } else {
                star.setImageResource(R.drawable.ic_star_empty); // Set empty star
            }
        }
    }

    private void openGallery() {
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
    }
}