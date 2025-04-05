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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.mapease.model.Review;
import com.example.mapease.adapter.AdminReviewAdapter;

import java.util.ArrayList;

public class Admin_ReviewFragment extends Fragment {
    FloatingActionButton fab;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth auth;
    ArrayList<Review> reviewList;
    AdminReviewAdapter userAdapter;
    SearchView searchView;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_admin_review, container, false);
        //fab = view.findViewById(R.id.fab);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://mapease22127072-default-rtdb.asia-southeast1.firebasedatabase.app");
        myRef = database.getReference("reviews");
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
                reviewList = new ArrayList<>();

                for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                    try {
                        Review review = reviewSnapshot.getValue(Review.class);
                        //if (user != null && user.getId().contentEquals(Id))
                        reviewList.add(review);
                    } catch (Exception e) {
                        Log.e("RetrieveReview", "Error parsing review", e);
                    }
                }

                for (Review review : reviewList)
                    Log.d("RetrieveReview", review.toString());

                // Update adapter with real reviews
                AdminReviewAdapter adapter = new AdminReviewAdapter(getContext(), reviewList);
                ListView listView = view.findViewById(R.id.listViewReview);
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Review clickedReview = reviewList.get(position);
                        Intent i = new Intent(getContext(), Admin_ReviewDetail.class);
                        i.putExtra("location", clickedReview.getLocationID());
                        i.putExtra("rating", clickedReview.getRating());
                        i.putExtra("createdAt", clickedReview.getCreateAt());
                        i.putExtra("content", clickedReview.getContent());
                        startActivity(i);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),
                        "Failed to load reviews: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
}