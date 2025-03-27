package com.example.habits;

import static android.text.format.DateUtils.isToday;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.core.net.ParseException;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList = new ArrayList<>();
    TextView menuTitle;

    ProgressBar progressBarTasks;
    TextView progressLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(this, taskList);
        recyclerView.setAdapter(adapter);
        menuTitle = findViewById(R.id.textMenuTitle);
        progressBarTasks = findViewById(R.id.progressBarTasks);
        progressLabel = findViewById(R.id.progressLabel);

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
        Chip chipTrash = findViewById(R.id.chipTrash);

        chipCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                chipArchived.setChecked(false);
                chipTrash.setChecked(false);
                displayFilteredTasks(dbHelper, 1);
            } else {
                displayTasks(dbHelper);
            }
        });

        chipArchived.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                chipCompleted.setChecked(false);
                chipTrash.setChecked(false);
                displayFilteredTasks(dbHelper, 2);
            } else {
                displayTasks(dbHelper);
            }
        });

        chipTrash.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                chipCompleted.setChecked(false);
                chipArchived.setChecked(false);
                displayFilteredTasks(dbHelper, 3);
            } else {
                displayTasks(dbHelper);
            }
        });

        progressBarTasks = findViewById(R.id.progressBarTasks);
        progressLabel = findViewById(R.id.progressLabel);

        //Log.d("FILTER", "Showing completed tasks");

        Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM tasks WHERE status = 0 OR status = 1", null);
//        while (cursor.moveToNext()) {
//            //Log.d("Task", "ID: " + cursor.getInt(0) + " Name: " + cursor.getString(1));
//        }
        cursor.close();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void showAddTaskDialog(DatabaseTask dbHelper) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Task");

        EditText taskInput = new EditText(this);

        EditText deadlineInput = new EditText(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(taskInput);
        layout.addView(deadlineInput);

        taskInput.setHint("Enter Task Name");
        taskInput.setInputType(InputType.TYPE_CLASS_TEXT);

        deadlineInput.setHint("Select Date & Time");
        deadlineInput.setFocusable(false);
        deadlineInput.setClickable(true);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String taskName = taskInput.getText().toString().trim();
            String deadline = deadlineInput.getText().toString().trim();
            if (deadline.isEmpty()) {
                Toast.makeText(this, "Please select a valid deadline!", Toast.LENGTH_SHORT).show();
                return;
            }

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

                        int taskId = dbHelper.getLastInsertedTaskId();

                        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                this,
                                taskId,
//                                (int) System.currentTimeMillis(),
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

        deadlineInput.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePicker = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        TimePickerDialog timePicker = new TimePickerDialog(this,
                                (timeView, hourOfDay, minute) -> {
                                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    calendar.set(Calendar.MINUTE, minute);

                                    if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                                        Toast.makeText(this, "⛔ Cannot select a past time!", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                    deadlineInput.setText(sdf.format(calendar.getTime()));
                                },
                                24, 0, true // Default 00:00 AM
                        );

                        if (isToday(calendar)) {
                            Calendar now = Calendar.getInstance();
                            timePicker.updateTime(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
                        }

                        timePicker.show();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
            datePicker.show();

        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();


    }

    private void displayTasks(DatabaseTask dbHelper) {
        taskList.clear();
        menuTitle.setVisibility(View.VISIBLE);
        progressBarTasks.setVisibility(View.VISIBLE);
        progressLabel.setVisibility(View.VISIBLE);
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM tasks WHERE status = 0", null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String deadline = cursor.getString(2);
            int taskStatus = cursor.getInt(3);
            taskList.add(new Task(id, name, deadline, taskStatus));
        }

        cursor.close();
        updateProgressBar(dbHelper);

        if (adapter == null) {
            adapter = new TaskAdapter(this, taskList);
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

//       Chip chipCompleted = findViewById(R.id.chip2);
//       Chip chipArchived = findViewById(R.id.chip);
//       Chip chipTrash = findViewById(R.id.chipTrash);
//
//        if (chipCompleted.isChecked()) {
//            displayFilteredTasks(dbHelper, 1);
//        } else if (chipArchived.isChecked()) {
//            displayFilteredTasks(dbHelper, 2);
//        } else if (chipTrash.isChecked()) {
//            displayFilteredTasks(dbHelper, 3);
//        } else {
//            displayTasks(dbHelper);
//        }

        if (newStatus == 1 || newStatus == 2 || newStatus == 3) {
            Intent intent = new Intent(this, ReminderNotification.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    taskId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
        }
        displayTasks(dbHelper);

    }

    private void displayFilteredTasks(DatabaseTask dbHelper, int status) {
        menuTitle.setVisibility(View.GONE);
        progressBarTasks.setVisibility(View.GONE);
        progressLabel.setVisibility(View.GONE);
        taskList.clear();
        Cursor cursor;

        if (status == 1) {
            cursor = dbHelper.getCompletedTasks();
        } else if (status == 2) {
            cursor = dbHelper.getArchivedTasks();
        } else if (status == 3) {
            cursor = dbHelper.getTrashedTasks();
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

            EditText deadlineInput = new EditText(this); // not deadLineInput
            deadlineInput.setHint("Select Date & Time");
            deadlineInput.setFocusable(false);
            deadlineInput.setClickable(true);
            layout.addView(deadlineInput);

            builder.setView(layout);

            deadlineInput.setOnClickListener(v -> {
                final Calendar calendar = Calendar.getInstance();

                DatePickerDialog datePicker = new DatePickerDialog(this,
                        (view, year, month, dayOfMonth) -> {
                            calendar.set(Calendar.YEAR, year);
                            calendar.set(Calendar.MONTH, month);
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                            TimePickerDialog timePicker = new TimePickerDialog(this,
                                    (timeView, hourOfDay, minute) -> {
                                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                        calendar.set(Calendar.MINUTE, minute);

                                        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                                            Toast.makeText(this, "⛔ Cannot select a past time!", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                        deadlineInput.setText(sdf.format(calendar.getTime()));
                                    },
                                    9, 0, true // Default time: 9:00 AM
                            );

                            if (isToday(calendar)) {
                                Calendar now = Calendar.getInstance();
                                timePicker.updateTime(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
                            }

                            timePicker.show();
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );

                datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
                datePicker.show();
            });


            builder.setPositiveButton("Update", (dialog, which) -> {
                String updatedName = taskInput.getText().toString();
                String updatedDeadline = deadlineInput.getText().toString();
                if (updatedDeadline.isEmpty()) {
                    Toast.makeText(this, "Please select a valid deadline!", Toast.LENGTH_SHORT).show();
                    return;
                }

                dbHelper.updateTask(task.getId(), updatedName, updatedDeadline);
                displayTasks(dbHelper);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                try {
                    Date date = sdf.parse(updatedDeadline);
                    if (date != null) {
                        long triggerTime = date.getTime();

                        Intent intent = new Intent(this, ReminderNotification.class);
                        intent.putExtra("taskName", updatedName);

                        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                this,
                                task.getId(), // same ID ensures replacement
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                        );

                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (alarmManager.canScheduleExactAlarms()) {
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                            } else {
                                Toast.makeText(this, "Exact alarms not permitted. Please allow in settings.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
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