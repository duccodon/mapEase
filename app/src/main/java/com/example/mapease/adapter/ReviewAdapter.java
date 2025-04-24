package com.example.mapease.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapease.MainActivity;
import com.example.mapease.R;
import com.example.mapease.ReportReview;
import com.example.mapease.model.Review;
import com.example.mapease.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ReviewAdapter extends ArrayAdapter<Review> {

    private final Context context;
    private final List<Review> reviews;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private DatabaseReference userRef, reportRef;
    private String currentUserID;

    public ReviewAdapter(Context context, List<Review> reviews) {
        super(context, 0, reviews);
        this.context = context;
        this.reviews = reviews;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.review_item, parent, false);
        }

        Review review = reviews.get(position);

        Log.d("ReviewData", review.getContent() + " " + review.getCreateAt() + " " + review.getLocationID() + " " + review.getLikes()
        + " " + review.getRating() + " " + review.getUserID());

        // Bind data to views
        TextView userTextView = itemView.findViewById(R.id.reviewUser);
        TextView locationTextView = itemView.findViewById(R.id.reviewLocation);
        RatingBar ratingBar = itemView.findViewById(R.id.reviewRating);
        TextView timeTextView = itemView.findViewById(R.id.reviewTime);
        TextView contentTextView = itemView.findViewById(R.id.reviewContent);
        ImageView likeIcon = itemView.findViewById(R.id.reviewLikeIcon);
        TextView likeCountTextView = itemView.findViewById(R.id.reviewLikeCount);
        RecyclerView imagesRecycler = itemView.findViewById(R.id.reviewImagesRecycler);
        ImageButton reportBtn = itemView.findViewById(R.id.report_button);

        // Set data
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        db = FirebaseDatabase.getInstance("https://mapease22127072-default-rtdb.asia-southeast1.firebasedatabase.app");
        userRef = db.getReference("user");
        reportRef = db.getReference("reports");

        setUsername(userTextView, review.getUserID());
        ratingBar.setRating(review.getRating());
        timeTextView.setText(formatDate(review.getCreateAt()));
        setLocation(review, locationTextView);
        contentTextView.setText(review.getContent());

        //checkReportButtonVisibility
        // Condition: Check if the review belongs to the current user
        if(review.getUserID().contentEquals(currentUserID))
            reportBtn.setVisibility(View.INVISIBLE);

        //new handle likes
        Map<String, Boolean> likes = review.getLikes();
            likeIcon.setVisibility(View.VISIBLE);
            likeCountTextView.setVisibility(View.VISIBLE);
            // Set like count
            likeCountTextView.setText(String.valueOf(likes.size() - 1));

            if (likes.containsKey(currentUserID)) {
                likeIcon.setImageResource(R.drawable.heart);
                likeIcon.setColorFilter(ContextCompat.getColor(context, R.color.red));
            } else {
                likeIcon.setImageResource(R.drawable.outline_heart);
                likeIcon.setColorFilter(ContextCompat.getColor(context, R.color.gray));
            }

        // Handle like icon click
        likeIcon.setOnClickListener(v -> {
            DatabaseReference likesRef = db
                    .getReference("reviews")
                    .child(review.getReviewId())
                    .child("likes")
                    .child(currentUserID);

            if (likes.containsKey(currentUserID)) {
                likesRef.removeValue();
            } else {
                likesRef.setValue(true);
            }
        });

        // Handle images
        List<String> base64Images = review.getImageUrls();
        if (base64Images != null && !base64Images.isEmpty()) {
            imagesRecycler.setVisibility(View.VISIBLE);
            ImageAdapter imageAdapter = new ImageAdapter(context, base64Images);
            imagesRecycler.setAdapter(imageAdapter);
        } else {
            imagesRecycler.setVisibility(View.GONE);
        }

        //Handle report
        reportBtn.setOnClickListener(v -> {
            Intent i = new Intent(context, ReportReview.class);
            i.putExtra("reviewId", review.getReviewId());
            i.putExtra("reporterId", currentUserID);
            if (context instanceof Activity) {
                ((Activity) context).startActivity(i);
            } else {
                Log.e("ReviewAdapter", "Context is not an Activity, cannot start ReportReview");
                Toast.makeText(context, "Cannot start report activity", Toast.LENGTH_SHORT).show();
            }
        });

        return itemView;
    }

    private void setUsername(TextView userTextView, String UID) {
        userRef.child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);

                    String username = user.getUsername();
                    userTextView.setText(username);
                    Log.d("UserData", "Username: " + username);
                } else {
                    Log.d("UserData", "User data doesn't exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UserData", "Error fetching user data: " + error.getMessage(), error.toException());
            }
        });
    }

    private String formatDate(String isoTime) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
            Date pastDate = isoFormat.parse(isoTime);
            Date now = new Date();
            long diffInMillis = now.getTime() - pastDate.getTime();

            long seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
            long hours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
            long days = TimeUnit.MILLISECONDS.toDays(diffInMillis);
            long weeks = days / 7;
            long months = days / 30;
            long years = days / 365;

            if (seconds < 60) {
                return "Just now";
            } else if (minutes < 60) {
                return minutes + "m ago";
            } else if (hours < 24) {
                return hours + "h ago";
            } else if (days < 7) {
                return days + "d ago";
            } else if (weeks < 4) {
                return weeks + "w ago";
            } else if (months < 12) {
                return months + "mo ago";
            } else {
                return years + "y ago";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return isoTime; //return origin if fail
        }
    }

    public interface LocationAddressCallback {
        void onAddressLoaded(String address);
        void onFailure(Exception e);
    }

    private void getLocationAddress(String locationID, LocationAddressCallback callback) {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.ADDRESS
        );

        PlacesClient placesClient = Places.createClient(context);
        FetchPlaceRequest request = FetchPlaceRequest.builder(locationID, fields).build();

        placesClient.fetchPlace(request).addOnSuccessListener(fullResponse -> {
            Place place = fullResponse.getPlace();
            callback.onAddressLoaded(place.getAddress());
        }).addOnFailureListener(e -> {
            Log.e("Review", "Error finding place: " + e.getMessage());
            callback.onFailure(e);
        });
    }

    private void setLocation(Review review, TextView locationTextView){
        getLocationAddress(review.getLocationID(), new LocationAddressCallback() {
            @Override
            public void onAddressLoaded(String address) {
                // Update UI on main thread
                ((Activity) context).runOnUiThread(() -> {
                    locationTextView.setText(address);
                });
            }

            @Override
            public void onFailure(Exception e) {
                ((Activity) context).runOnUiThread(() -> {
                    locationTextView.setText("Address not available");
                });
            }
        });

    }

    public float calculateAverageRating() {
        if (reviews == null || reviews.isEmpty()) {
            return 0f;
        }

        float sum = 0f;
        for (Review review : reviews) {
            sum += review.getRating();
        }
        return sum / reviews.size();
    }
    // ImageAdapter class for online URL image
    /*private static class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
        private final Context context;
        private final List<String> imageUrls;

        public ImageAdapter(Context context, List<String> imageUrls) {
            this.context = context;
            this.imageUrls = imageUrls;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_review_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Glide.with(context)
                    .load(imageUrls.get(position))
                    .placeholder(R.drawable.default_location)
                    .error(R.drawable.broken_image)
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return imageUrls.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
            }
        }
    }*/

    private static class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
        private final Context context;
        private final List<String> base64Images;

        public ImageAdapter(Context context, List<String> base64Images) {
            this.context = context;
            this.base64Images = base64Images;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_review_image, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String base64Image = base64Images.get(position);

            try {
                // Remove data URI prefix if present
                String pureBase64;
                if (base64Image.startsWith("data:image")) {
                    pureBase64 = base64Image.split(",")[1];
                } else {
                    pureBase64 = base64Image;
                }

                // Decode Base64 string
                byte[] decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT);

                // Create bitmap with memory optimization
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

                // Calculate inSampleSize to reduce memory usage
                options.inSampleSize = calculateInSampleSize(options, 500, 500);
                options.inJustDecodeBounds = false;

                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);

                if (bitmap != null) {
                    holder.imageView.setImageBitmap(bitmap);
                } else {
                    Log.e("ImageAdapter", "Failed to decode bitmap");
                    holder.imageView.setImageResource(R.drawable.broken_image);
                }
            } catch (IllegalArgumentException e) {
                Log.e("ImageAdapter", "Invalid Base64 string", e);
                holder.imageView.setImageResource(R.drawable.broken_image);
            } catch (Exception e) {
                Log.e("ImageAdapter", "Error decoding image", e);
                holder.imageView.setImageResource(R.drawable.broken_image);
            }
        }

        // Helper method to calculate sample size for memory optimization
        private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                while ((halfHeight / inSampleSize) >= reqHeight
                        && (halfWidth / inSampleSize) >= reqWidth) {
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

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
            }
        }
    }
}
