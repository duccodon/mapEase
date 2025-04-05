package com.example.mapease;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.mapease.model.User;

import java.text.DateFormat;
import java.util.Calendar;

public class Admin_UploadActivity extends AppCompatActivity {
    ImageView uploadImage;
    Button saveButton;
    EditText uploadUsername, uploadBio, uploadEmail, uploadRole;
    String imageURL;
    Uri uri;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_upload);
        database = FirebaseDatabase.getInstance("https://mapease22127072-default-rtdb.asia-southeast1.firebasedatabase.app");
        myRef = database.getReference("user");
        auth = FirebaseAuth.getInstance();

        uploadImage = findViewById(R.id.uploadImage);
        uploadBio = findViewById(R.id.uploadBio);
        uploadUsername = findViewById(R.id.uploadUsername);
        uploadEmail = findViewById(R.id.uploadEmail);
        uploadRole = findViewById(R.id.uploadRole);
        saveButton = findViewById(R.id.saveButton);

        Intent i = getIntent();
        String bioStr = i.getStringExtra("bio");
        String usernameStr = i.getStringExtra("username");
        String emailStr = i.getStringExtra("email");
        String roleStr = i.getStringExtra("role");
        String userId = i.getStringExtra("Id");

        uploadBio.setText(bioStr);
        uploadUsername.setText(usernameStr);
        uploadEmail.setText(emailStr);
        uploadRole.setText(roleStr);

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            uri = data.getData();
                            uploadImage.setImageURI(uri);
                        } else {
                            Toast.makeText(Admin_UploadActivity.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPicker = new Intent(Intent.ACTION_PICK);
                photoPicker.setType("image/*");
                activityResultLauncher.launch(photoPicker);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 1. Get input values
                String email = uploadEmail.getText().toString().trim();
                String username = uploadUsername.getText().toString().trim();
                String bio = uploadBio.getText().toString().trim();
                String role = uploadRole.getText().toString().trim();

                // 2. Validate input
                if (email.isEmpty()) {
                    uploadEmail.setError("Email cannot be empty");
                    return;
                }
                if (username.isEmpty()) {
                    uploadUsername.setError("Username cannot be empty");
                    return;
                }

                //Create new user
                /*
                 auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // 4. Get user ID
                                    String userId = auth.getCurrentUser().getUid();

                                    /* // 5. Upload image to Firebase Storage (if selected)
                                    if (uri != null) {
                                        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                                                .child("user_images/" + userId);
                                        storageRef.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                                        String imageURL = uri.toString();

                                                        // 6. Create User object
                                                        User user = new User();
                                                        user.setEmail(email);
                                                        user.setUsername(username);
                                                        user.setBio(bio.isEmpty() ? "Did not add" : bio);
                                                        user.setAvatar(imageURL);
                                                        user.setRole("user");

                                                        // 7. Save to Firebase Database
                                                        myRef.child(userId).setValue(user)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            Toast.makeText(Admin_UploadActivity.this, "User saved successfully", Toast.LENGTH_SHORT).show();
                                                                            finish(); // Exit activity
                                                                        } else {
                                                                            Toast.makeText(Admin_UploadActivity.this, "Failed to save: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                    });
                                                } else {
                                                    Toast.makeText(Admin_UploadActivity.this, "Image upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {
                // 6. No image, save user directly
                User user = new User();
                user.setEmail(email);
                user.setUsername(username);
                user.setBio(bio.isEmpty() ? "Did not add" : bio);
                user.setAvatar("@drawable/profile_user"); // Default avatar
                user.setRole("user");

                // 7. Save to Firebase Database
                myRef.child(userId).setValue(user)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Admin_UploadActivity.this, "User saved successfully", Toast.LENGTH_SHORT).show();
                                    finish(); // Exit activity
                                } else {
                                    Toast.makeText(Admin_UploadActivity.this, "Failed to save: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                Toast.makeText(Admin_UploadActivity.this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }); */

                // 3. Update user in Firebase
                User updatedUser = new User();
                updatedUser.setId(userId);
                updatedUser.setUsername(username);
                updatedUser.setEmail(email);
                updatedUser.setBio(bio);
                updatedUser.setAvatar("newAvatarUrl");
                updatedUser.setRole(role);

                myRef.child(userId).setValue(updatedUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Admin_UploadActivity.this, "User saved successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), AdminActivity.class));
                                } else {
                                    Toast.makeText(Admin_UploadActivity.this, "Failed to save: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}
