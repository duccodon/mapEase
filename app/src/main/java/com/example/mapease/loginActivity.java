package com.example.mapease;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
}