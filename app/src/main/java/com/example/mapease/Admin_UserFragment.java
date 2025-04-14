package com.example.mapease;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import com.example.mapease.model.User;
import com.example.mapease.adapter.UserAdapter;

public class Admin_UserFragment extends Fragment {
    FloatingActionButton fab;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth auth;
    ArrayList<User> userList;
    UserAdapter userAdapter;
    SearchView searchView;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_admin_user, container, false);
        //fab = view.findViewById(R.id.fab);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://mapease22127072-default-rtdb.asia-southeast1.firebasedatabase.app");
        myRef = database.getReference("user");
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
                userList = new ArrayList<>();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    try {
                        User user = userSnapshot.getValue(User.class);
                        //if (user != null && user.getId().contentEquals(Id))
                        user.setId(userSnapshot.getKey());
                        userList.add(user);
                    } catch (Exception e) {
                        Log.e("RetrieveUser", "Error parsing user", e);
                    }
                }

                /* for (User user : userList)
                    Log.d("RetrieveUser", user.toString()); */

                // Update adapter with real users
                userAdapter = new UserAdapter(getContext(), userList);
                ListView listView = view.findViewById(R.id.listView);
                listView.setAdapter(userAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        User clickedUser = userList.get(position);
                        Intent i = new Intent(getContext(), Admin_UserDetail.class);
                        i.putExtra("username", clickedUser.getUsername());
                        i.putExtra("bio", clickedUser.getBio());
                        i.putExtra("email", clickedUser.getEmail());
                        i.putExtra("role", clickedUser.getRole());
                        i.putExtra("Id", clickedUser.getId());
                        startActivity(i);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(),
                        "Failed to load users: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });



        return view;
    }

}