package com.example.mapease;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

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
        List<String> imageUrls = review.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            imagesRecycler.setVisibility(View.VISIBLE);
            ImageAdapter imageAdapter = new ImageAdapter(context, imageUrls);
            imagesRecycler.setAdapter(imageAdapter);
        } else {
            imagesRecycler.setVisibility(View.GONE);
        }

        return itemView;
    }


    // ImageAdapter class
    private static class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
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
    }
}
