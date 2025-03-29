package com.example.mapease;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        Thread thread = new Thread()
        {
            @Override
            public void run() {
                try {
                    sleep(3000);
                    startActivity(new Intent(SplashScreen.this, MainActivity.class));
                    finish();
                }catch (Exception e) {

                }
            }
        }; thread.start();
    }
}