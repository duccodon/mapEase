package com.example.mapease;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.mapease.adapter.AdminReportAdapter;
import com.example.mapease.model.ReportReview;
import com.example.mapease.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Admin_ReportFragment extends Fragment {

    FloatingActionButton fab;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth auth;
    ArrayList<ReportReview> reportList;
    AdminReportAdapter reportAdapter;
    SearchView searchView;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_admin_report, container, false);
        //fab = view.findViewById(R.id.fab);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://mapease22127072-default-rtdb.asia-southeast1.firebasedatabase.app");
        myRef = database.getReference("reports");
        //searchView = view.findViewById(R.id.search);
        //searchView.clearFocus();
        /* fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), Admin_UploadActivity.class));
            }
        }); */

        //load users
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reportList = new ArrayList<>();

                for (DataSnapshot reportSnapshot : snapshot.getChildren()) {
                    try {
                        ReportReview report = reportSnapshot.getValue(ReportReview.class);
                        //if (user != null && user.getId().contentEquals(Id))
                        report.setId(reportSnapshot.getKey());
                        reportList.add(report);
                    } catch (Exception e) {
                        Log.e("RetrieveReview", "Error parsing review", e);
                    }
                }

                /* for (ReportReview report : reportList)
                    Log.d("RetrieveReport", report.toString()); */

                // Update adapter with real reviews
                reportAdapter = new AdminReportAdapter(getContext(), reportList);
                ListView listView = view.findViewById(R.id.listViewReport);
                listView.setAdapter(reportAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ReportReview clickedReport = reportList.get(position);
                        Intent i = new Intent(getContext(), Admin_ReportDetail.class);
                        i.putExtra("createdAt", clickedReport.getCreatedAt());
                        i.putExtra("description", clickedReport.getDescription());
                        i.putExtra("reporterId", clickedReport.getReporterId());
                        i.putExtra("title", clickedReport.getTitle());
                        i.putExtra("reviewId", clickedReport.getReviewId());
                        i.putExtra("Id", clickedReport.getId());
                        startActivity(i);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),
                        "Failed to load reports: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
}