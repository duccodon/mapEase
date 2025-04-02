package com.example.mapease.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import com.example.mapease.R;
import com.example.mapease.model.Review;

import java.util.List;
import java.util.Map;

public class ReviewAdapter extends ArrayAdapter<Review> {

    private final Context context;
    private final List<Review> reviews;

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

        // Set data
        userTextView.setText(review.getUserID());
        ratingBar.setRating(review.getRating());
        timeTextView.setText(review.getCreateAt());
        locationTextView.setText(review.getLocationID());
        contentTextView.setText(review.getContent());


        String currentUserID = "ZbfpPC8DkegTta8kYEjdORcw6cs2";//test user lien ket vs data ben yourProfileActivity.java

        //new handle likes
        Map<String, Boolean> likes = review.getLikes();

            likeIcon.setVisibility(View.VISIBLE);
            likeCountTextView.setVisibility(View.VISIBLE);

            // Set like count (number of entries in the map)
            likeCountTextView.setText(String.valueOf(likes.size()));

            if (likes.containsKey(currentUserID)) {
                likeIcon.setImageResource(R.drawable.heart);
                likeIcon.setColorFilter(ContextCompat.getColor(context, R.color.red));
            } else {
                likeIcon.setImageResource(R.drawable.outline_heart);
                likeIcon.setColorFilter(ContextCompat.getColor(context, R.color.gray));
            }

        // Handle like icon click, will fix when handle with firebase
        /*likeIcon.setOnClickListener(v -> {
            DatabaseReference likesRef = FirebaseDatabase.getInstance()
                    .getReference("reviews")
                    .child(review.getReviewId())
                    .child("likes")
                    .child(currentUserId);

            if (likes != null && likes.containsKey(currentUserId)) {
                // Unlike: Remove user from likes
                likesRef.removeValue();
            } else {
                // Like: Add user to likes
                likesRef.setValue(true);
            }
        });*/

        // Handle images
        List<String> base64Images = review.getImageUrls();
        if (base64Images != null && !base64Images.isEmpty()) {
            imagesRecycler.setVisibility(View.VISIBLE);
            ImageAdapter imageAdapter = new ImageAdapter(context, base64Images);
            imagesRecycler.setAdapter(imageAdapter);
        } else {
            imagesRecycler.setVisibility(View.GONE);
        }

        return itemView;
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
