package com.example.mapease;

import android.content.Intent;
import android.os.Bundle;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.example.mapease.model.ReportReview;
import com.google.firebase.database.ValueEventListener;

public class ReportSubmit extends AppCompatActivity {
    TextView title;
    TextView description;
    ImageView puzzle;
    ImageButton btnBack;
    Button btnSubmit;
    String reportId, titleStr, descriptionStr, reviewId, reporterId, createdAt;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report_submit);

        database = FirebaseDatabase.getInstance("https://mapease22127072-default-rtdb.asia-southeast1.firebasedatabase.app");
        myRef = database.getReference("reports");

        title = findViewById(R.id.title_text);
        description = findViewById(R.id.description_text);
        puzzle = findViewById(R.id.puzzle_icon);
        btnBack = findViewById(R.id.reportSubmitBackBtn);
        btnSubmit = findViewById(R.id.submit_button);

        Map<String, Integer> reportDetails = new HashMap<>();
        reportDetails.put("Off topic", R.drawable.report_off_topic);
        reportDetails.put("Spam", R.drawable.report_spam);
        reportDetails.put("Conflict of interest", R.drawable.report_conflict);
        reportDetails.put("Profanity", R.drawable.report_profanity);
        reportDetails.put("Bullying or harassment", R.drawable.report_bullying);
        reportDetails.put("Discrimination or hate speech", R.drawable.report_discrimination);
        reportDetails.put("Personal information", R.drawable.report_personal_information);
        reportDetails.put("Not helpful", R.drawable.report_not_helpful);

        Intent intent = getIntent();
        titleStr = intent.getStringExtra("title");
        descriptionStr = intent.getStringExtra("description");
        reviewId = intent.getStringExtra("reviewId");
        reporterId = intent.getStringExtra("reporterId");
        reportId = reporterId + "_" + reviewId;

        if(reportDetails.get(titleStr) == null)
            puzzle.setImageResource(R.drawable.vietnamese);
        else
            puzzle.setImageResource(reportDetails.get(titleStr));

        title.setText(titleStr);
        description.setText(descriptionStr);

        createdAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()).format(new Date());

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Disable the submit button to prevent multiple clicks
                btnSubmit.setEnabled(false);

                // Check if report already exists
                myRef.child(reportId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Report already exists
                            Toast.makeText(getApplicationContext(), "You have already reported this review", Toast.LENGTH_SHORT).show();
                            // Re-enable the submit button
                            btnSubmit.setEnabled(true);
                        } else {
                            // Report does not exist, proceed to submit
                            ReportReview report = new ReportReview();
                            report.setId(reportId);
                            report.setReporterId(reporterId);
                            report.setReviewId(reviewId);
                            report.setTitle(titleStr);
                            report.setDescription(descriptionStr);
                            report.setCreatedAt(createdAt);

                            myRef.child(reportId).setValue(report).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Report submitted successfully", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Failed to submit report: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    // Re-enable the submit button
                                    btnSubmit.setEnabled(true);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "Error checking report status: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        // Re-enable the submit button
                        btnSubmit.setEnabled(true);
                    }
                });
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}