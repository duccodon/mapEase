package com.example.mapease;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Admin_UserDetail extends AppCompatActivity {
    ImageView img;
    TextView email, bio, username, role;
    FloatingActionButton deleteButton, editButton;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth auth;
    String emailStr, bioStr, usernameStr, roleStr, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_user_detail);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://mapease22127072-default-rtdb.asia-southeast1.firebasedatabase.app");
        myRef = database.getReference("user");

        email = findViewById(R.id.detailEmail);
        bio = findViewById(R.id.detailBio);
        username = findViewById(R.id.detailUsername);
        role = findViewById(R.id.detailRole);
        deleteButton = findViewById(R.id.deleteButton);
        editButton = findViewById(R.id.editButton);

        Intent intent = getIntent();
        userId = intent.getStringExtra("Id");
        emailStr = intent.getStringExtra("email");
        email.setText(emailStr);
        roleStr = intent.getStringExtra("role");
        role.setText(roleStr);
        bioStr = intent.getStringExtra("bio");
        bio.setText(bioStr);
        usernameStr = intent.getStringExtra("username");
        username.setText(usernameStr);


        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef.child(userId).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            auth.getUid();
                            Toast.makeText(Admin_UserDetail.this, "User data deleted", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), AdminActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(Admin_UserDetail.this, "Deletion failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Admin_UploadActivity.class);
                i.putExtra("email", emailStr);
                i.putExtra("role", roleStr);
                i.putExtra("username", usernameStr);
                i.putExtra("bio", bioStr);
                i.putExtra("Id", userId);
                startActivity(i);
            }
        });
    }
}