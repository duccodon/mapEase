package com.example.mapease.adapter;


import android.app.AlertDialog;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.mapease.SaveLocation;
import com.example.mapease.MainActivity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mapease.R;
import com.example.mapease.model.Review;
import com.example.mapease.model.favoriteLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SaveLocationAdapter extends ArrayAdapter<favoriteLocation> {

    private Context context;
    private List<favoriteLocation> favoriteLocations;
    private OnLocationRemovedListener listener;

    public  interface  OnLocationRemovedListener {
        void onLocationRemoved(String locationId);
    }

    public void setOnLocationRemovedListener(OnLocationRemovedListener listener) {
        this.listener = listener;
    }

    private static final int MAX_ITEMS_TO_DISPLAY = 5;


    public SaveLocationAdapter(Context context, List<favoriteLocation> locationList) {
        super(context, 0, locationList);
        this.context = context;
        this.favoriteLocations = locationList;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_save_location, parent, false);
        }

        favoriteLocation fl = getItem(position);

        TextView nameTextView = convertView.findViewById(R.id.saveName);
        TextView addressTextView = convertView.findViewById(R.id.saveAddress);
        ImageView imageView = convertView.findViewById(R.id.saveImageView);
        Button removeButton = convertView.findViewById(R.id.removeButton);

        if (fl != null) {
            nameTextView.setText(fl.getLocationName());
            addressTextView.setText(fl.getLocationAddress());

            if (fl.getImageUrls() != null && !fl.getImageUrls().isEmpty()) {
                byte[] imageBytes = Base64.decode(fl.getImageUrls(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageView.setImageBitmap(bitmap);
            } else {
                Log.d("SaveLocationAdapter", "Image URL is null or empty");
            }
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fl == null) {
                    Log.d("SaveLocationAdapter", "favoriteLocation is null");
                    return;
                }

                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("selectedPlaceID", fl.getLocationID());
                intent.putExtra("selectedPlaceName", fl.getLocationName());
                intent.putExtra("selectedLatitude", fl.getLocationLatitude());
                intent.putExtra("selectedLongitude", fl.getLocationLongitude());
                intent.putExtra("context", "viewSaveLocation");

                context.startActivity(intent);
            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fl == null) {
                    Log.d("SaveLocationAdapter", "favoriteLocation is null");
                    return;
                }

                new AlertDialog.Builder(context)
                        .setTitle("Remove Location")
                        .setMessage("Are you sure you want to remove this location?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            Intent intent = new Intent(context, SaveLocation.class);
                            intent.putExtra("selectedPlaceID", fl.getLocationID());
                            intent.putExtra("context", "remove");

                            context.startActivity(intent);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            dialog.dismiss(); // User cancelled
                        })
                        .show();
            }
        });

        return convertView;
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
            return isoTime;
        }
    }



}
