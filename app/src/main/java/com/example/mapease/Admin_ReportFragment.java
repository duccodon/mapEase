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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.widget.SearchView;

import android.widget.Spinner;
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
    ArrayList<ReportReview> filteredList;
    AdminReportAdapter reportAdapter;
    SearchView searchView;
    private Spinner stateFilterSpinner;
    private int selectedState = -1; // -1 for "All", 0 for Pending, 1 for Accept, 2 for Decline
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
        stateFilterSpinner = view.findViewById(R.id.stateFilterSpinner);

        // Initialize ListView and lists
        ListView listView = view.findViewById(R.id.listViewReport);
        reportList = new ArrayList<>();
        filteredList = new ArrayList<>();
        reportAdapter = new AdminReportAdapter(getContext(), filteredList);
        listView.setAdapter(reportAdapter);

        // Initialize SearchView
        searchView = view.findViewById(R.id.adminReportSearch);
        searchView.clearFocus(); // Prevent immediate keyboard popup
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // Not handling submit action
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterReports(newText);
                return true;
            }
        });

        // Initialize Spinner
        ArrayList<String> stateOptions = new ArrayList<>();
        stateOptions.add("All");
        stateOptions.add("Pending");
        stateOptions.add("Accept");
        stateOptions.add("Decline");
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, stateOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateFilterSpinner.setAdapter(spinnerAdapter);

        // Spinner selection listener
        stateFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedState = position - 1; // Map position to state: -1 (All), 0 (Pending), 1 (Accept), 2 (Decline)
                filterReports(searchView.getQuery().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedState = -1; // Default to All
                filterReports(searchView.getQuery().toString());
            }
        });

        //load users
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reportList.clear();
                filteredList.clear();

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

                // Update filtered list with all reports initially
                filteredList.addAll(reportList);
                reportAdapter.notifyDataSetChanged();

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ReportReview clickedReport = filteredList.get(position);
                        Intent i = new Intent(getContext(), Admin_ReportDetail.class);
                        i.putExtra("createdAt", clickedReport.getCreatedAt());
                        i.putExtra("description", clickedReport.getDescription());
                        i.putExtra("reporterId", clickedReport.getReporterId());
                        i.putExtra("title", clickedReport.getTitle());
                        i.putExtra("reviewId", clickedReport.getReviewId());
                        i.putExtra("Id", clickedReport.getId());
                        i.putExtra("state", String.valueOf(clickedReport.getState()));
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
    private void filterReports(String query) {
        filteredList.clear();
        String lowerCaseQuery = query != null ? query.toLowerCase().trim() : "";

        for (ReportReview report : reportList) {
            boolean matchesQuery = query.isEmpty() || (report.getId() != null && report.getId().toLowerCase().contains(lowerCaseQuery));
            boolean matchesState = selectedState == -1 || report.getState() == selectedState;

            if (matchesQuery && matchesState) {
                filteredList.add(report);
            }
        }
        reportAdapter.notifyDataSetChanged();
    }
}