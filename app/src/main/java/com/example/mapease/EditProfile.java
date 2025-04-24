package com.example.mapease;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class EditProfile extends AppCompatActivity {

    ImageButton returnProfile;

    EditText editUsername;
    EditText editBio;
    EditText editEmail;
    ImageView editAvatar;
    Button saveButton;
    FirebaseAuth auth;
    FirebaseUser user;
    AuthCredential credential;
    DatabaseReference userRef;

    ImageView editIconOverlay;

    private static final int PICK_IMAGE_REQUEST = 1;
    String avatarBase64 = "";
    String username;
    String bio;
    String email;
    EditText password;

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

        editUsername = findViewById(R.id.editUsername);
        editBio = findViewById(R.id.editBio);
        editEmail = findViewById(R.id.editEmail);
        editAvatar = findViewById(R.id.editAvatar);
        saveButton = findViewById(R.id.saveProfileButton);
        password = findViewById(R.id.editPassword);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        String userId = user.getUid();
        userRef = FirebaseDatabase.getInstance("https://mapease22127072-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("user").child(userId);

        saveButton.setOnClickListener(v -> saveProfile());
        editIconOverlay = findViewById(R.id.editIconOverlay);

        // Simulate hover behavior with touch
        editAvatar.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    editIconOverlay.setVisibility(View.VISIBLE);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    editIconOverlay.setVisibility(View.GONE);
                    pickImageFromGallery(); // if you still want to pick image
                    break;
            }
            return true;
        });


        userRef.get().addOnSuccessListener(snapshot -> {
            username = snapshot.child("username").getValue(String.class);
            editUsername.setText(username);
            bio = snapshot.child("bio").getValue(String.class);
            editBio.setText(bio);
            email = snapshot.child("email").getValue(String.class);
            editEmail.setText(email);

            String base64Avatar = snapshot.child("avatar").getValue(String.class);
            if (base64Avatar != null && !base64Avatar.isEmpty() && !base64Avatar.startsWith("@drawable/")) {
                try {
                    byte[] decodedString = Base64.decode(base64Avatar, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    editAvatar.setImageBitmap(decodedByte);
                    avatarBase64 = base64Avatar;
                } catch (Exception e) {
                    editAvatar.setImageResource(R.drawable.profile_user);
                }
            } else {
                editAvatar.setImageResource(R.drawable.profile_user);
            }
        });
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                editAvatar.setImageBitmap(bitmap);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] imageBytes = baos.toByteArray();
                avatarBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveProfile() {
        String newEmail = editEmail.getText().toString();
        String newBio = editBio.getText().toString();
        String newUsername = editUsername.getText().toString();

        if (TextUtils.isEmpty(username)) {
            editUsername.setError("Username is required");
            return;
        }

        // Convert ImageView to Base64
        editAvatar.setDrawingCacheEnabled(true);
        editAvatar.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) editAvatar.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // reduce size
        byte[] imageBytes = baos.toByteArray();
        String avatarBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        credential = EmailAuthProvider.getCredential(email, password.getText().toString());
        Log.d("UID", credential.toString());

        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.verifyBeforeUpdateEmail(newEmail)
                        .addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                Toast.makeText(getApplicationContext(),
                                        "Verification email sent to " + newEmail + ". Please verify before it becomes active.",
                                        Toast.LENGTH_LONG).show();

                                userRef.child("username").setValue(newUsername);
                                userRef.child("bio").setValue(newBio);
                                userRef.child("email").setValue(newEmail);
                                userRef.child("avatar").setValue(avatarBase64);

                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Email update failed: " + updateTask.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                Log.e("EmailUpdate", updateTask.getException().getMessage());
                            }
                        });
            } else {
                Toast.makeText(getApplicationContext(),
                        "Reauthentication failed: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
