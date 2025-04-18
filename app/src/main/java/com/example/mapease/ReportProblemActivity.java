package com.example.mapease;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import  com.example.mapease.model.ReportProblem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportProblemActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;//myRef

    String context;

    private TextView placeInfo;
    private EditText extraInput;
    private Button submitReport;

    private CheckBox cbTrash, cbLights, cbGraffiti, cbRoad, cbOther;

    private String placeName, placeAddress, locationId;
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

        cbTrash = findViewById(R.id.cb_trash);
        cbLights = findViewById(R.id.cb_lights);
        cbGraffiti = findViewById(R.id.cb_graffiti);
        cbRoad = findViewById(R.id.cb_road);
        cbOther = findViewById(R.id.cb_other);


        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://mapease22127072-default-rtdb.asia-southeast1.firebasedatabase.app");
        mDatabase = database.getReference("reportProblem");

        //get the current user ID
        String userId = auth.getCurrentUser().getUid();

        // Get the location ID from the intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            locationId = extras.getString("locationID");
            placeName = getIntent().getStringExtra("selectedName");
            placeAddress = getIntent().getStringExtra("selectedAddress");
            locationLatitude = getIntent().getDoubleExtra("selectedLatitude", 0.0);
            locationLongitude = getIntent().getDoubleExtra("selectedLongitude", 0.0);

        }

        placeInfo.setText("Reporting for:\n" + placeName + "\n" + placeAddress);


        submitReport.setOnClickListener(v -> {
            List<String> selectedIssues = new ArrayList<>();
            if (cbTrash.isChecked()) selectedIssues.add("Trash Overflow");
            if (cbLights.isChecked()) selectedIssues.add("Broken Street Light");
            if (cbGraffiti.isChecked()) selectedIssues.add("Graffiti");
            if (cbRoad.isChecked()) selectedIssues.add("Road Damage");
            if (cbOther.isChecked()) selectedIssues.add("Other");

            if (selectedIssues.isEmpty()) {
                Toast.makeText(this, "Select at least one issue", Toast.LENGTH_SHORT).show();
                return;
            }

            String comments = extraInput.getText().toString().trim();
            String createdAt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()).format(new Date());

            String reportID = userId + "_" + locationId;

            ReportProblem reportProblem = new ReportProblem(placeName, placeAddress, selectedIssues, comments, createdAt,
                    reportID, userId, locationId, locationLatitude, locationLongitude);

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
