    package com.example.mapease;

    import android.content.Intent;
    import android.graphics.drawable.ColorDrawable;
    import android.os.Bundle;
    import android.text.TextUtils;
    import android.util.Log;
    import android.util.Patterns;
    import android.view.View;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.activity.EdgeToEdge;
    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AlertDialog;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.graphics.Insets;
    import androidx.core.view.ViewCompat;
    import androidx.core.view.WindowInsetsCompat;

    import com.example.mapease.model.User;
    import com.google.android.gms.tasks.OnCompleteListener;
    import com.google.android.gms.tasks.OnFailureListener;
    import com.google.android.gms.tasks.OnSuccessListener;
    import com.google.android.gms.tasks.Task;
    import com.google.firebase.FirebaseApp;
    import com.google.firebase.auth.AuthResult;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.auth.FirebaseAuthException;
    import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
    import com.google.firebase.auth.FirebaseAuthInvalidUserException;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.ValueEventListener;

    import java.util.regex.PatternSyntaxException;

    public class loginActivity extends AppCompatActivity {

        private FirebaseAuth auth;
        private FirebaseDatabase database;
        private DatabaseReference myRef;
        private EditText emailOrUsername, password;
        private TextView signupRedirect, forgotpassword;
        private Button signinButton;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);

            setContentView(R.layout.activity_login);
            /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });*/

            auth = FirebaseAuth.getInstance();
            database = FirebaseDatabase.getInstance("https://mapease22127072-default-rtdb.asia-southeast1.firebasedatabase.app");
            myRef = database.getReference("user");

            emailOrUsername = findViewById(R.id.signin_emailUsername);
            password = findViewById(R.id.signin_password);
            signinButton = findViewById(R.id.signin_button);
            signupRedirect = findViewById(R.id.singUpRedirect);
            forgotpassword = findViewById(R.id.forgotPassword);

            /*signinButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    Log.e("FirebaseDebug", "Database Ref: " + myRef.toString());
                    Toast.makeText(loginActivity.this, "Database URL: " + myRef.toString(), Toast.LENGTH_SHORT).show();

                    myRef.setValue("hello world")
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("FirebaseDebug", "✅ Data written successfully");
                                    Toast.makeText(loginActivity.this, "Data written successfully", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("FirebaseDebug", "❌ Error writing data: " + e.getMessage());
                                    Toast.makeText(loginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                }
            });*/
            signinButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    String email = emailOrUsername.getText().toString().trim();
                    String pass = password.getText().toString().trim();

                    if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        login(email, pass);
                    }else if (email.isEmpty()) {
                        emailOrUsername.setError("Email cannot be empty");
                    }else {
                        myRef.orderByChild("username").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String dbEmail = "";
                                    for (DataSnapshot child : snapshot.getChildren()) {
                                        dbEmail = child.child("email").getValue(String.class);
                                        break;
                                    }

                                    if (!dbEmail.isEmpty()) {
                                        login(dbEmail, pass);
                                    } else {
                                        Toast.makeText(loginActivity.this, "Username not linked to any email", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(loginActivity.this, "Invalid email or username", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                Log.e("FIREBASE", "Error: " + error.getMessage());
                            }
                        });
                    }
                }
            });

            signupRedirect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    startActivity(new Intent(loginActivity.this, signUpActivity.class));
                }
            });

            forgotpassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view){
                    AlertDialog.Builder builder = new AlertDialog.Builder(loginActivity.this);
                    View dialogView = getLayoutInflater().inflate(R.layout.forgot_password, null);
                    EditText emailReset = dialogView.findViewById(R.id.resetPass_email);

                    builder.setView(dialogView);
                    AlertDialog dialog = builder.create();

                    dialogView.findViewById(R.id.resetPassButton).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String resetPassEmail = emailReset.getText().toString();

                            if (TextUtils.isEmpty(resetPassEmail) && !Patterns.EMAIL_ADDRESS.matcher(resetPassEmail).matches()){
                                Toast.makeText(loginActivity.this, "Enter your email address to reset your password", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            auth.sendPasswordResetEmail(resetPassEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(loginActivity.this, "Reset password link has been sent to your email address", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }else{
                                        Toast.makeText(loginActivity.this, "Failed to reset your password", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });

                    dialogView.findViewById(R.id.returnSigninButton).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });


                    //make background transparent
                    if(dialog.getWindow() != null){
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
                    }


                    dialog.show();
                }
            });
        }

        private void login (String email, String pass){
            if (!pass.isEmpty()) {
                auth.signInWithEmailAndPassword(email, pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        String uid = auth.getCurrentUser().getUid();
                        DatabaseReference roleRef = myRef.child(uid).child("role");

                        roleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String role = snapshot.getValue(String.class);
                                if (role == null) role = "user"; // fallback nếu không có role

                                Toast.makeText(loginActivity.this, "Login successfully", Toast.LENGTH_SHORT).show();

                                Intent intent;
                                if (role.equals("admin")) {
                                    intent = new Intent(loginActivity.this, AdminActivity.class);
                                } else {
                                    intent = new Intent(loginActivity.this, MainActivity.class);
                                    intent.putExtra("context", "loginByUser");
                                    intent.putExtra("user_type", role);
                                }
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(loginActivity.this, "Error getting user role", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener(){
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMess = "";

                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            myRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    boolean exists = snapshot.exists();
                                    String msg = exists ? "Incorrect password" : "Invalid email";
                                    Toast.makeText(loginActivity.this, msg, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    Log.e("FIREBASE", "Error: " + error.getMessage());
                                }
                            });
                        } else {
                            Toast.makeText(loginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else {
                password.setError("Password cannot be empty");
            }
        }
    }
