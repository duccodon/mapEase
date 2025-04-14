package com.example.mapease.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mapease.R;
import com.example.mapease.model.ReportReview;
import com.example.mapease.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AdminReportAdapter extends ArrayAdapter<ReportReview> {
    Context context;
    private ArrayList<ReportReview> reportsArrayList;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private HashMap<String, User> usersMap;
    public AdminReportAdapter(Context context, ArrayList<ReportReview> reportsArrayList) {
        super(context, R.layout.admin_report_list_item, reportsArrayList);
        this.context = context;
        this.reportsArrayList = reportsArrayList;
        database = FirebaseDatabase.getInstance("https://mapease22127072-default-rtdb.asia-southeast1.firebasedatabase.app");
        myRef = database.getReference("user");
        usersMap = new HashMap<>();
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot userSnapshot : snapshot.getChildren()) {
                    try {
                        User user = userSnapshot.getValue(User.class);
                        //if (user != null && user.getId().contentEquals(Id))
                        user.setId(userSnapshot.getKey());
                        usersMap.put(user.getId(), user);
                    } catch (Exception e) {
                        Log.e("RetrieveUser", "Error parsing user", e);
                    }
                }
                notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),
                        "Failed to load users: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class viewHolder {
        TextView reportTitle;
        TextView reportDescription;
        TextView reportReporterName;
        TextView reportTime;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ReportReview report = getItem(position);
        viewHolder myViewHolder;
        final View result;

        if(convertView == null){
            myViewHolder = new viewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(
                    R.layout.admin_report_list_item,
                    parent,
                    false
            );
            //myViewHolder.userImage = convertView.findViewById(R.id.imageView);
            myViewHolder.reportTitle = convertView.findViewById(R.id.reportTitle);
            myViewHolder.reportDescription = convertView.findViewById(R.id.reportDescription);
            myViewHolder.reportReporterName = convertView.findViewById(R.id.reportReporterName);
            myViewHolder.reportTime = convertView.findViewById(R.id.reportCreatedAt);
            convertView.setTag(myViewHolder);
        }
        else{
            myViewHolder = (viewHolder) convertView.getTag();
        }

        myViewHolder.reportTitle.setText(report.getTitle());
        myViewHolder.reportTime.setText(formatDate(report.getCreatedAt()));
        myViewHolder.reportDescription.setText(report.getDescription());
        User user = usersMap.get(report.getReporterId());
        if (user != null) {
            myViewHolder.reportReporterName.setText(user.getUsername());
        }

        result = convertView;
        return result;
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
}
