package com.example.mapease;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import com.example.mapease.model.ReportProblem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ReportProblemActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;//myRef

    String context;

    private TextView placeInfo;
    private EditText extraInput;
    private Button submitReport;

    private  CheckBox cb_accident, cb_construction, cb_congestion, cb_flood, cb_pothole, cb_crime;

    ImageView backBtn;

    private String placeName;
    private  double locationLatitude;
    private  double locationLongitude;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_problem);

        placeInfo = findViewById(R.id.place_info);
        extraInput = findViewById(R.id.extra_input);
        submitReport = findViewById(R.id.submit_report);

        cb_accident = findViewById(R.id.cb_accident);
        cb_construction = findViewById(R.id.cb_construction);
        cb_congestion = findViewById(R.id.cb_congestion);
        cb_flood = findViewById(R.id.cb_flood);
        cb_pothole = findViewById(R.id.cb_pothole);
        cb_crime = findViewById(R.id.cb_crime);
        backBtn = findViewById(R.id.btn_back_reportProblem);


        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://mapease22127072-default-rtdb.asia-southeast1.firebasedatabase.app");
        mDatabase = database.getReference("reportProblem");

        //get the current user ID
        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        // Get the location ID from the intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            placeName = getIntent().getStringExtra("selectedName");
            locationLatitude = getIntent().getDoubleExtra("selectedLatitude", 0.0);
            locationLongitude = getIntent().getDoubleExtra("selectedLongitude", 0.0);

        }

        placeInfo.setText("Reporting for:\n" + placeName);

        backBtn.setOnClickListener(v -> {
            finish();
        });


        submitReport.setOnClickListener(v -> {
            List<String> selectedIssues = new ArrayList<>();
            if (cb_accident.isChecked()) {
                selectedIssues.add("Traffic Accident");
            }
            if (cb_construction.isChecked()) {
                selectedIssues.add("Road Construction / Maintenance");
            }
            if (cb_congestion.isChecked()) {
                selectedIssues.add("Heavy Traffic");
            }
            if (cb_flood.isChecked()) {
                selectedIssues.add("Flood / Bad Weather");
            }
            if (cb_pothole.isChecked()) {
                selectedIssues.add("Pothole / Road Damage");
            }
            if (cb_crime.isChecked()) {
                selectedIssues.add("Unsafe Area");
            }

            if (selectedIssues.isEmpty()) {
                Toast.makeText(this, "Select at least one issue", Toast.LENGTH_SHORT).show();
                return;
            }

            String comments = extraInput.getText().toString().trim();
            String createdAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()).format(new Date());

            String reportID = mDatabase.push().getKey();

            ReportProblem reportProblem = new ReportProblem(placeName, selectedIssues, comments, createdAt,
                    reportID, userId, locationLatitude, locationLongitude);

            mDatabase.child(reportID).setValue(reportProblem)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ReportProblemActivity.this, "Report submitted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ReportProblemActivity.this, "Failed to submit report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

            finish();
        });
    }
}
