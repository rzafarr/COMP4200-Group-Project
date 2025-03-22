package com.example.habits;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import androidx.core.net.ParseException;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.app.AlertDialog;
import android.content.DialogInterface;
import com.google.android.material.chip.Chip;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        DatabaseTask dbHelper = new DatabaseTask(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.close();

        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(view -> {
            showAddTaskDialog(dbHelper);
        });

        displayTasks(dbHelper);
        Chip chipCompleted = findViewById(R.id.chip2);
        Chip chipArchived = findViewById(R.id.chip);

        chipCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                chipArchived.setChecked(false);
                displayFilteredTasks(dbHelper, 1);
            } else {
                displayTasks(dbHelper);
            }
        });

        chipArchived.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                chipCompleted.setChecked(false);
                displayFilteredTasks(dbHelper, 2);
            } else {
                displayTasks(dbHelper);
            }
        });

        Cursor cursor = dbHelper.getAllTasks();
        while (cursor.moveToNext()) {
            Log.d("Task", "ID: " + cursor.getInt(0) + " Name: " + cursor.getString(1));
        }
        cursor.close();


    }

    @SuppressLint("ScheduleExactAlarm")
    private void showAddTaskDialog(DatabaseTask dbHelper) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Task");

        EditText taskInput = new EditText(this);
        taskInput.setHint("Enter Task Name");
        taskInput.setInputType(InputType.TYPE_CLASS_TEXT);

        EditText deadlineInput = new EditText(this);
        deadlineInput.setHint("Enter Date & Time (YYYY-MM-DD hh:mm)");
        deadlineInput.setInputType(InputType.TYPE_CLASS_TEXT);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(taskInput);
        layout.addView(deadlineInput);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String taskName = taskInput.getText().toString().trim();
            String deadline = deadlineInput.getText().toString().trim();

            if (!taskName.isEmpty()) {
                dbHelper.addTask(taskName, deadline);
                Toast.makeText(this, "Task Added!", Toast.LENGTH_SHORT).show();
                displayTasks(dbHelper);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

                try {

                    Date date = sdf.parse(deadline);
                    if (date != null) {
                        long triggerTime = date.getTime();

                        //long triggerTime = System.currentTimeMillis() + 10000; //Testing Purpose

                        Intent intent = new Intent(this, ReminderNotification.class);
                        intent.putExtra("taskName", taskName);

                        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                this,
                                (int) System.currentTimeMillis(),
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                        );

                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(this, "Task name cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });


        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();


    }

    private void displayTasks(DatabaseTask dbHelper) {
        taskList.clear();
        Cursor cursor = dbHelper.getAllTasks();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String deadline = cursor.getString(2);
            int taskStatus = cursor.getInt(3);
            taskList.add(new Task(id, name, deadline, taskStatus));
        }

        cursor.close();

        if (adapter == null) {
            adapter = new TaskAdapter(this, taskList);
            recyclerView = findViewById(R.id.recyclerViewTasks);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }


    }

    public void updateTaskStatus(int taskId, int newStatus) {
        DatabaseTask dbHelper = new DatabaseTask(this);
        dbHelper.updateTaskStatus(taskId, newStatus);
        displayTasks(dbHelper);
    }

    private void displayFilteredTasks(DatabaseTask dbHelper, int status) {
        taskList.clear();
        Cursor cursor;

        if (status == 1) {
            cursor = dbHelper.getCompletedTasks();
        } else if (status == 2) {
            cursor = dbHelper.getArchivedTasks();
        } else {
            cursor = dbHelper.getAllTasks();
        }

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String deadline = cursor.getString(2);
            int taskStatus = cursor.getInt(3);
            taskList.add(new Task(id, name, deadline, taskStatus));
        }
        cursor.close();

        if (adapter == null) {

            adapter = new TaskAdapter(this, taskList);
            recyclerView = findViewById(R.id.recyclerViewTasks);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }
        public void editTaskDialog(Task task) {
        DatabaseTask dbHelper = new DatabaseTask(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Task");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText taskInput = new EditText(this);
        taskInput.setHint("Task Name");
        taskInput.setText(task.getName());
        layout.addView(taskInput);

        final EditText deadlineInput = new EditText(this);
        deadlineInput.setHint("Deadline (yyyy-MM-dd)");
        deadlineInput.setText(task.getDeadline());
        layout.addView(deadlineInput);

        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String updatedName = taskInput.getText().toString();
            String updatedDeadline = deadlineInput.getText().toString();
            dbHelper.updateTask(task.getId(), updatedName, updatedDeadline);
            displayTasks(dbHelper);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }




}