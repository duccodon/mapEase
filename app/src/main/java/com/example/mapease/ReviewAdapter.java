package com.example.mapease;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

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

        // Bind data to views
        TextView titleTextView = itemView.findViewById(R.id.reviewTitle);
        TextView subtitleTextView = itemView.findViewById(R.id.reviewSubtitle);
        TextView locationTextView = itemView.findViewById(R.id.reviewLocation);
        RatingBar ratingBar = itemView.findViewById(R.id.reviewRating);
        TextView timeTextView = itemView.findViewById(R.id.reviewTime);
        TextView commentTextView = itemView.findViewById(R.id.reviewComment);
        TextView viewsTextView = itemView.findViewById(R.id.reviewViews);
        TextView reactTextView = itemView.findViewById(R.id.reviewReact);
        LinearLayout likeLayout = itemView.findViewById(R.id.reviewLikeLayout);
        TextView likeCountTextView = itemView.findViewById(R.id.reviewLikeCount);

        titleTextView.setText(review.getTitle());
        subtitleTextView.setText(review.getSubtitle());
        locationTextView.setText(review.getLocation());
        ratingBar.setRating(review.getRating());
        timeTextView.setText(review.getTime());
        commentTextView.setText(review.getComment());
        viewsTextView.setText(review.getViews());
        reactTextView.setText(review.getReact());

        // Handle likes (optional, for restaurant reviews)
        if (review.getLikes() != null) {
            likeLayout.setVisibility(View.VISIBLE);
            likeCountTextView.setText(String.valueOf(review.getLikes()));
        } else {
            likeLayout.setVisibility(View.GONE);
        }

        return itemView;
    }
}
