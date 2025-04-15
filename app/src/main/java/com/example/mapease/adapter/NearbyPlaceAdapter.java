package com.example.mapease.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mapease.R;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.List;

public class NearbyPlaceAdapter extends RecyclerView.Adapter<NearbyPlaceAdapter.PlaceViewHolder> {
    private Context context;
    private List<Place> places;
    private final OnPlaceClickListener listener;
    public interface OnPlaceClickListener {
        void onPlaceClick(Place place);
    }

    public NearbyPlaceAdapter(Context context, List<Place> places, OnPlaceClickListener listener) {
        this.context = context;
        this.places = places;
        this.listener = listener;

    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = places.get(position);

        holder.tvName.setText(place.getDisplayName());
        holder.tvAddress.setText(place.getFormattedAddress());
        holder.tvPhone.setText("Phone: " + (place.getPhoneNumber() != null ? place.getPhoneNumber() : "N/A"));
        holder.tvWebsite.setText("Website: " + (place.getWebsiteUri() != null ? place.getWebsiteUri().toString() : "N/A"));

        if (place.getOpeningHours() != null && place.getOpeningHours().getWeekdayText() != null) {
            holder.tvHours.setText("Hours:\n" + TextUtils.join("\n", place.getOpeningHours().getWeekdayText()));
        } else {
            holder.tvHours.setText("Hours: N/A");
        }

        // Load meta photo nếu có
        List<PhotoMetadata> photos = place.getPhotoMetadatas();
        if (photos != null && !photos.isEmpty()) {
            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photos.get(0)).build();
            PlacesClient client = Places.createClient(context);


            client.fetchPhoto(photoRequest).addOnSuccessListener(photoResponse -> {
                holder.imgPlace.setImageBitmap(photoResponse.getBitmap());
            }).addOnFailureListener(e -> {
                holder.imgPlace.setImageResource(R.drawable.default_location); // fallback
            });
        } else {
            holder.imgPlace.setImageResource(R.drawable.default_location); // fallback
        }

        holder.itemView.setOnClickListener(v -> listener.onPlaceClick(place));
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public static class PlaceViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAddress, tvPhone, tvWebsite, tvHours;
        ImageView imgPlace;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvWebsite = itemView.findViewById(R.id.tvWebsite);
            tvHours = itemView.findViewById(R.id.tvHours);
            imgPlace = itemView.findViewById(R.id.imgPlace);
        }
    }
}

