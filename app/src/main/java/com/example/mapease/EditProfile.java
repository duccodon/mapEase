package com.example.mapease;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class EditProfile extends AppCompatActivity {

    ImageButton returnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        returnProfile = findViewById(R.id.returnProfile);
        returnProfile.setOnClickListener(v -> {
            finish();
        });

        /*editUsername = findViewById(R.id.editUsername);
        editBio = findViewById(R.id.editBio);
        editAvatar = findViewById(R.id.editAvatar);
        saveButton = findViewById(R.id.saveProfileButton);

        auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance("https://mapease22127072-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("user").child(userId);

        saveButton.setOnClickListener(v -> saveProfile());*/
    }

    /*private void saveProfile() {
        String username = editUsername.getText().toString().trim();
        String bio = editBio.getText().toString().trim();
        String avatar = editAvatar.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            editUsername.setError("Username is required");
            return;
        }

        userRef.child("username").setValue(username);
        userRef.child("bio").setValue(bio);
        userRef.child("avatar").setValue(avatar)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(editProfile.this, "Profile updated", Toast.LENGTH_SHORT).show();
                    finish(); // go back
                })
                .addOnFailureListener(e -> Toast.makeText(editProfile.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }*/
}
