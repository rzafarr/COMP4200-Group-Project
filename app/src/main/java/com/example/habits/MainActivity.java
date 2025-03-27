package com.example.habits;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // create the activity
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = Objects.requireNonNull(navHostFragment).getNavController();

        // set up the navigation view
        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnItemSelectedListener(item -> {
            navigationView.getMenu().findItem(R.id.item_1).setIcon(R.drawable.home_outline);
            navigationView.getMenu().findItem(R.id.item_2).setIcon(R.drawable.settings_outline);

            switch (Objects.requireNonNull(item.getTitle()).toString()) {
                case "Home":
                    item.setIcon(R.drawable.home_filled);
                    navController.navigate(R.id.habitListFragment);
                    break;
                case "Settings":
                    item.setIcon(R.drawable.settings_filled);
                    navController.navigate(R.id.settingsFragment);
                    break;
            }
            return true;
        });
    }
}