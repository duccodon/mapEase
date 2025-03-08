package com.example.mapease;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.PatternSyntaxException;

public class loginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
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
        emailOrUsername = findViewById(R.id.signin_emailUsername);
        password = findViewById(R.id.signin_password);
        signinButton = findViewById(R.id.signin_button);
        signupRedirect = findViewById(R.id.singUpRedirect);
        forgotpassword = findViewById(R.id.forgotPassword);

        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                String email = emailOrUsername.getText().toString().trim();
                String pass = password.getText().toString().trim();

                if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (!pass.isEmpty()) {
                        auth.signInWithEmailAndPassword(email, pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess (AuthResult authResult){
                                Toast.makeText(loginActivity.this, "Login successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(loginActivity.this, MainActivity.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener(){
                            @Override
                            public void onFailure(@NonNull Exception e){
                                Toast.makeText(loginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else {
                        password.setError("Password cannot be empty");
                    }
                }else if (email.isEmpty()) {
                    emailOrUsername.setError("Email cannot be empty");
                }else {
                    emailOrUsername.setError("Please enter a valid email");
                }
            }
        });

        signupRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                startActivity(new Intent(loginActivity.this, signUpActivity.class));
            }
        });
    }
}