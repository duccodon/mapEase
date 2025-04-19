package com.example.mapease;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mapease.model.User;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
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

public class Admin_ReportDetail extends AppCompatActivity {
    TextView title, createdAt, description, reporterName;
    FloatingActionButton acceptBtn, declineBtn;
    private FirebaseDatabase database;
    private DatabaseReference reportRef, userRef;
    private FirebaseAuth auth;
    String reportId, createdAtStr, descriptionStr, reporterIdStr, titleStr, reviewIdStr, stateStr;
    ImageButton backBtn;
    ArrayList<User> userList;
    Button viewReviewBtn;
    ImageView reportState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_report_detail);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://mapease22127072-default-rtdb.asia-southeast1.firebasedatabase.app");
        reportRef = database.getReference("reports");
        userRef = database.getReference("user");

        title = findViewById(R.id.detailReportTitle);
        createdAt = findViewById(R.id.detailCreatedAt);
        description = findViewById(R.id.detailDescription);
        reporterName = findViewById(R.id.detailReporterName);
        acceptBtn = findViewById(R.id.acceptReportButton);
        declineBtn = findViewById(R.id.declineReportButton);
        backBtn = findViewById(R.id.backButtonDetailReport);
        viewReviewBtn = findViewById(R.id.viewReviewButton);
        reportState = findViewById(R.id.reportDetailStatus);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList = new ArrayList<>();

                for(DataSnapshot userSnapshot : snapshot.getChildren()) {
                    try {
                        User user = userSnapshot.getValue(User.class);
                        //if (user != null && user.getId().contentEquals(Id))
                        user.setId(userSnapshot.getKey());
                        userList.add(user);
                    } catch (Exception e) {
                        Log.e("RetrieveUser", "Error parsing user", e);
                    }
                }

                Intent intent = getIntent();
                createdAtStr = intent.getStringExtra("createdAt");
                createdAt.setText(formatDate(createdAtStr));
                descriptionStr = intent.getStringExtra("description");
                description.setText(descriptionStr);
                reporterIdStr = intent.getStringExtra("reporterId");
                for(User user : userList){
                    if(user != null && user.getId().contentEquals(reporterIdStr)){
                        reporterName.setText(user.getUsername());
                    }
                }
                titleStr = intent.getStringExtra("title");
                title.setText(titleStr);
                reviewIdStr = intent.getStringExtra("reviewId");
                reportId = intent.getStringExtra("Id");
                stateStr = intent.getStringExtra("state");
                int state = Integer.parseInt(stateStr);
                updateStateUI(state);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(),
                        "Failed to load users: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateReportState(1); // Accept
            }
        });

        declineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateReportState(2); // Decline
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        viewReviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Admin_ReviewDetail.class);
                i.putExtra("reviewId", reviewIdStr);
                startActivity(i);
            }
        });

    }

    private void updateReportState(int newState) {
        if (reportId == null || reportId.isEmpty()) {
            Toast.makeText(this, "Invalid report ID", Toast.LENGTH_SHORT).show();
            return;
        }

        int currentState = Integer.parseInt(stateStr);
        if (currentState == newState) {
            Toast.makeText(this, "Report is already " + (newState == 1 ? "accepted" : "declined"), Toast.LENGTH_SHORT).show();
            return;
        }

        // Update report state
        reportRef.child(reportId).child("state").setValue(newState)
                .addOnSuccessListener(aVoid -> {
                    stateStr = String.valueOf(newState);
                    updateStateUI(newState);
                    acceptBtn.setEnabled(false);
                    declineBtn.setEnabled(false);

                    if (newState == 1) { // Accept: Delete the review
                        if (reviewIdStr == null || reviewIdStr.isEmpty()) {
                            Toast.makeText(this, "Invalid review ID", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        DatabaseReference reviewRef = database.getReference("reviews").child(reviewIdStr);
                        reviewRef.removeValue()
                                .addOnSuccessListener(aVoid2 -> {
                                    Toast.makeText(this, "Report accepted and review deleted successfully", Toast.LENGTH_SHORT).show();
                                    // Optionally, finish() to return to the previous screen
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to delete review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Report declined successfully", Toast.LENGTH_SHORT).show();
                        // Optionally, finish() to return to the previous screen
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void updateStateUI(int state) {
        switch (state) {
            case 0:
                reportState.setImageResource(R.drawable.ic_report_status);
                acceptBtn.setEnabled(true);
                declineBtn.setEnabled(true);
                break;
            case 1:
                reportState.setImageResource(R.drawable.ic_baseline_accept_24);
                acceptBtn.setEnabled(false);
                declineBtn.setEnabled(false);
                break;
            case 2:
                reportState.setImageResource(R.drawable.ic_decline);
                acceptBtn.setEnabled(false);
                declineBtn.setEnabled(false);
                break;
            default:
                reportState.setImageResource(R.drawable.ic_report_status);
                acceptBtn.setEnabled(true);
                declineBtn.setEnabled(true);
                break;
        }
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