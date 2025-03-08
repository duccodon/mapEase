package com.example.mapease;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class signUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText signupEmail, signupUsername, signupPassword;
    private Button signupButton;
    private TextView signinRedirect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/

        auth = FirebaseAuth.getInstance();
        signupEmail = findViewById(R.id.signup_email);
        signupUsername = findViewById(R.id.signup_username);
        signupPassword = findViewById(R.id.signup_password);
        signupButton = findViewById(R.id.signup_button);
        signinRedirect = findViewById(R.id.signInRedirect);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                String email = signupEmail.getText().toString().trim();
                String username = signupUsername.getText().toString().trim();
                String pass = signupPassword.getText().toString().trim();

                if (email.isEmpty()){
                    signupEmail.setError("Email cannot be empty");
                }
                if (username.isEmpty()){
                    signupEmail.setError("Username cannot be empty");
                }
                if (pass.isEmpty()){
                    signupEmail.setError("Password cannot be empty");
                }else {
                    auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>(){
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(signUpActivity.this, "Sign up successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(signUpActivity.this, loginActivity.class));
                            }else{
                                Toast.makeText(signUpActivity.this, "Sign up failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        signinRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                startActivity(new Intent(signUpActivity.this, loginActivity.class));
            }
        });
    }
}