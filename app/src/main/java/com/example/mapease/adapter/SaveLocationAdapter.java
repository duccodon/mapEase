package com.example.mapease.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mapease.R;
import com.example.mapease.model.favoriteLocation;

import java.util.List;

public class SaveLocationAdapter extends  RecyclerView.Adapter<SaveLocationAdapter.ViewHolder> {

    private Context context;
    private List<favoriteLocation> favoriteLocations;

    private static final int MAX_ITEMS_TO_DISPLAY = 5;


    public SaveLocationAdapter(Context context, List<favoriteLocation> locationList) {
        this.context = context;
        this.favoriteLocations = locationList;
    }

    @NonNull
    @Override
    public SaveLocationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_save_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SaveLocationAdapter.ViewHolder holder, int position) {
        if (position < MAX_ITEMS_TO_DISPLAY && position < favoriteLocations.size()) {
            favoriteLocation fl = favoriteLocations.get(position);

            holder.nameTextView.setText(fl.getLocationName());
            holder.addressTextView.setText(fl.getLocationAddress());

            Glide.with(context)
                    .load(fl.getImageUrls())
                    .error(R.drawable.ic_error)
                    .into(holder.imageView);
        }

    }

    @Override
    public int getItemCount() {
        return favoriteLocations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, addressTextView;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.place_name);
            addressTextView = itemView.findViewById(R.id.place_address);
            imageView = itemView.findViewById(R.id.place_image);
        }
    }
}
