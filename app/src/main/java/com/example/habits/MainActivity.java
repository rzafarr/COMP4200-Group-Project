package com.example.habits;

import static android.text.format.DateUtils.isToday;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // create the activity
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        // set up the navigation view
        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnItemSelectedListener(item -> {
            navigationView.getMenu().findItem(R.id.item_1).setIcon(R.drawable.home_outline);
            navigationView.getMenu().findItem(R.id.item_2).setIcon(R.drawable.settings_outline);

            switch (item.getTitle().toString()) {
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
    private boolean selectedDateIsToday(Calendar calendar) {
        Calendar today = Calendar.getInstance();
        return today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isChipCompletedChecked() {
        Chip chipCompleted = findViewById(R.id.chip2);
        return chipCompleted != null && chipCompleted.isChecked();
    }

    private boolean isChipArchivedChecked() {
        Chip chipArchived = findViewById(R.id.chip);
        return chipArchived != null && chipArchived.isChecked();
    }

    private boolean isChipTrashChecked() {
        Chip chipTrash = findViewById(R.id.chipTrash);
        return chipTrash != null && chipTrash.isChecked();
    }

    private boolean isToday(Calendar calendar) {
        Calendar today = Calendar.getInstance();
        return today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR);
    }

    private void updateProgressBar(DatabaseTask dbHelper) {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM tasks WHERE status = 0 OR status = 1", null);
        int todayTotal = 0;
        int todayCompleted = 0;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        while (cursor.moveToNext()) {
            String deadline = cursor.getString(2);
            int status = cursor.getInt(3);
            if (deadline != null && deadline.length() >= 10) {
                String taskDate = deadline.substring(0, 10);
                if (taskDate.equals(today)) {
                    todayTotal++;
                    if (status == 1) todayCompleted++;
                }
            }
        }
        cursor.close();

        if (todayTotal > 0) {
            int percent = (int) (((double) todayCompleted / todayTotal) * 100);
            progressBarTasks.setProgress(percent);
            progressLabel.setText("📊 Today's Progress: " + todayCompleted + " / " + todayTotal);
            progressBarTasks.setVisibility(View.VISIBLE);
            progressLabel.setVisibility(View.VISIBLE);
        } else {
            progressBarTasks.setVisibility(View.GONE);
            progressLabel.setVisibility(View.GONE);
        }
    }




}