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

import com.example.mapease.model.Review;

import java.util.List;

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
        RatingBar ratingBar = itemView.findViewById(R.id.reviewRating);
        TextView timeTextView = itemView.findViewById(R.id.reviewTime);
        TextView locationTextView = itemView.findViewById(R.id.reviewLocation);
        TextView commentTextView = itemView.findViewById(R.id.reviewComment);
        ImageView likeIcon = itemView.findViewById(R.id.reviewLikeIcon);
        TextView likeCountTextView = itemView.findViewById(R.id.reviewLikeCount);

        // Set data
        userTextView.setText(review.getUserID()); // Using getUserID() since there's no getUser()
        ratingBar.setRating(review.getRating());

        // Format time if needed (you might want to format this from timestamp to "X days ago")
        timeTextView.setText(review.getCreateAt());

        // Using getLocationID() since there's no getLocation()
        // You might want to convert locationID to a display name
        locationTextView.setText(review.getLocationID());

        commentTextView.setText(review.getContent()); // Using getContent() instead of getComment()

        // Handle likes
        if (review.getLikes() != null && review.getLikes() > 0) {
            likeCountTextView.setText(String.valueOf(review.getLikes()));
            likeCountTextView.setVisibility(View.VISIBLE);
            likeIcon.setVisibility(View.VISIBLE);
        } else {
            likeCountTextView.setVisibility(View.GONE);
            likeIcon.setVisibility(View.GONE);
        }

        return itemView;
    }
}
