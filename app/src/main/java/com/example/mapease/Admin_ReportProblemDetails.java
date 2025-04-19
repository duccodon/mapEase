package com.example.mapease;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mapease.Utils.MapUtils;
import com.example.mapease.model.User;
import com.example.mapease.model.HazardReport;

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
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Admin_ReportProblemDetails extends AppCompatActivity {
    TextView title, createdAt, description, reporterName;
    FloatingActionButton acceptBtn, declineBtn;
    private FirebaseDatabase database;
    private DatabaseReference reportRef, userRef;
    private DatabaseReference hazardReportRef;
    private FirebaseAuth auth;
    String reportId, createdAtStr, descriptionStr, reporterIdStr, titleStr, issueType;
    double latitude, longitude;

    ImageButton backBtn;
    ArrayList<User> userList;
    Button viewReviewBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_report_detail);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://mapease22127072-default-rtdb.asia-southeast1.firebasedatabase.app");

        reportRef = database.getReference("reportProblem");
        userRef = database.getReference("user");
        hazardReportRef = database.getReference("hazardReport");

        title = findViewById(R.id.detailReportTitle);
        createdAt = findViewById(R.id.detailCreatedAt);
        description = findViewById(R.id.detailDescription);
        reporterName = findViewById(R.id.detailReporterName);
        acceptBtn = findViewById(R.id.acceptReportButton);
        declineBtn = findViewById(R.id.declineReportButton);
        backBtn = findViewById(R.id.backButtonDetailReport);
        viewReviewBtn = findViewById(R.id.viewReviewButton);

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
                descriptionStr = intent.getStringExtra("description");
                reporterIdStr = intent.getStringExtra("reporterId");
                for(User user : userList){
                    if(user != null && user.getId().contentEquals(reporterIdStr)){
                        reporterName.setText(user.getUsername());
                    }
                }


                titleStr = intent.getStringExtra("title");
                reportId = intent.getStringExtra("reportId");
                issueType = intent.getStringExtra("issueType");
                latitude = intent.getDoubleExtra("latitude", 0.0);
                longitude = intent.getDoubleExtra("longitude", 0.0);

                title.setText(titleStr);
                description.setText(descriptionStr);
                createdAt.setText(formatDate(createdAtStr));
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
                // Chuẩn bị dữ liệu
                String createdAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()).format(new Date());
                String reporterId = FirebaseAuth.getInstance().getCurrentUser() != null
                        ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                        : "anonymous";

                HazardReport report = new HazardReport(
                        issueType,
                        descriptionStr,
                        latitude,
                        longitude,
                        createdAt,
                        reporterIdStr
                );


                // Tạo key mới
                String key = hazardReportRef.push().getKey();
                if (key != null) {
                    hazardReportRef.child(key).setValue(report)
                            .addOnSuccessListener(aVoid -> {
                                //Toast.makeText(Admin_ReportProblemDetails.this, "Report submitted!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(Admin_ReportProblemDetails.this, "Failed to submit: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }

                reportRef.child(reportId).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            // Xóa thành công
                            Toast.makeText(Admin_ReportProblemDetails.this, "Submit successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            // Xóa thất bại
                            Toast.makeText(Admin_ReportProblemDetails.this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        declineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportRef.child(reportId).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            // Xóa thành công
                            Toast.makeText(Admin_ReportProblemDetails.this, "delete successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            // Xóa thất bại
                            Toast.makeText(Admin_ReportProblemDetails.this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
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
}
