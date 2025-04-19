package com.example.mapease;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

public class AdminActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Toolbar toolbar = findViewById(R.id.toolbar); //Ignore red line errors
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.admin_open_menu,
                R.string.admin_close_menu);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Admin_UserFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_user);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.nav_user)
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Admin_UserFragment()).commit();
        else if(id == R.id.nav_problem)
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Admin_ReportProblemFragment()).commit();
        else if(id == R.id.nav_report)
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Admin_ReportFragment()).commit();
        else if(id == R.id.nav_map)
        {
            Intent intent = new Intent(AdminActivity.this, MainActivity.class);
            intent.putExtra("user_type", "admin");
            intent.putExtra("context", "drawHazardMarkers"); // để phân biệt bên MainActivity
            startActivity(intent);
        }
        else if(id == R.id.nav_logout)
        {
            Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), loginActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}