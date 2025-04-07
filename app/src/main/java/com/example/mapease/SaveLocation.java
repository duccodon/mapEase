package com.example.mapease;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class SaveLocation extends AppCompatActivity {

    // Firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;//myRef

    private static final int PICK_IMAGE_REQUEST = 1; // Request code for selecting an image
    private static final int PERMISSION_REQUEST_CODE = 2; // Permission request code
    String context;
    private String locationId;
    private String locationName;

    private String locationAddress;

    private String locationNotes;

    private String locationType;

    private double locationLatitude;

    private double locationLongitude;

    private String createAt;

    private List<String> imageUrls;

    private String userID;

    private String favoriteId;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference("favoriteLocations");

//        // Initialize UI elements
//        EditText locationNameEditText = findViewById(R.id.location_name_edit_text);
//        EditText locationAddressEditText = findViewById(R.id.location_address_edit_text);
//        EditText locationNotesEditText = findViewById(R.id.location_notes_edit_text);
//        EditText locationTypeEditText = findViewById(R.id.location_type_edit_text);
//        Button saveButton = findViewById(R.id.save_button);
//
//        // Set up save button click listener
//        saveButton.setOnClickListener(v -> {
//            saveLocation();
//        });
    }
    
}
