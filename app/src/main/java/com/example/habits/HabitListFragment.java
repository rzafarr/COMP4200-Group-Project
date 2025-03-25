package com.example.habits;

import static android.content.Context.ALARM_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HabitListFragment extends Fragment {
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList = new ArrayList<>();
    private View view;

    public HabitListFragment() {
        // Required empty public constructor
    }

    public static HabitListFragment newInstance() {
        HabitListFragment fragment = new HabitListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_habit_list, container, false);

        // initialize variables
        recyclerView = view.findViewById(R.id.recyclerViewTasks);

        // initialize database helper
        DatabaseTask dbHelper = new DatabaseTask(view.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.close();

        // initialize fab
        ExtendedFloatingActionButton fab = view.findViewById(R.id.addFab);
        fab.setOnClickListener(fabView -> {
            showAddTaskDialog(dbHelper);
        });

        updateTaskList(dbHelper, 0);
//        Chip chipCompleted = view.findViewById(R.id.chip2);
//        Chip chipArchived = view.findViewById(R.id.chip);
//
//        chipCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (isChecked) {
//                chipArchived.setChecked(false);
//                updateTaskList(dbHelper, 1);
//            } else {
//                updateTaskList(dbHelper, 0);
//            }
//        });
//
//        chipArchived.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            if (isChecked) {
//                chipCompleted.setChecked(false);
//                updateTaskList(dbHelper, 2);
//            } else {
//                updateTaskList(dbHelper, 0);
//            }
//        });

        Cursor cursor = dbHelper.getAllTasks();
        while (cursor.moveToNext()) {
            Log.d("Task", "ID: " + cursor.getInt(0) + " Name: " + cursor.getString(1));
        }
        cursor.close();

        // return the inflated view
        return view;
    }

    private void updateTaskList(DatabaseTask dbHelper, int status) {
        taskList.clear();

        Cursor cursor;
        switch (status) {
            case 1:
                cursor = dbHelper.getCompletedTasks();
                break;
            case 2:
                cursor = dbHelper.getArchivedTasks();
                break;
            default:
                cursor = dbHelper.getAllTasks();
                break;
        }

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String deadline = cursor.getString(2);
            int taskStatus = cursor.getInt(3);
            taskList.add(new Task(id, name, deadline, taskStatus));
            Log.d("Task", "ID: " + id + " Name: " + name);
        }
        cursor.close();

        if (adapter != null) {
            // update the adapter
            adapter.notifyDataSetChanged();
        } else {
            // create the adapter if it doesn't exist
            adapter = new TaskAdapter(view.getContext(), taskList);
            
            recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
            recyclerView.setAdapter(adapter);
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private void showAddTaskDialog(DatabaseTask dbHelper) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Add New Task");

        EditText taskInput = new EditText(view.getContext());
        taskInput.setHint("Enter Task Name");
        taskInput.setInputType(InputType.TYPE_CLASS_TEXT);

        EditText deadlineInput = new EditText(view.getContext());
        deadlineInput.setHint("Enter Date & Time (YYYY-MM-DD hh:mm)");
        deadlineInput.setInputType(InputType.TYPE_CLASS_TEXT);

        LinearLayout layout = new LinearLayout(view.getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(taskInput);
        layout.addView(deadlineInput);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String taskName = taskInput.getText().toString().trim();
            String deadline = deadlineInput.getText().toString().trim();

            Log.d("Task", "Task added: " + taskName);

            if (!taskName.isEmpty()) {
                dbHelper.addTask(taskName, deadline);

                Log.d("Task", "Task added: " + taskName);

                Toast.makeText(view.getContext(), "Task Added!", Toast.LENGTH_SHORT).show();
                updateTaskList(dbHelper, 0);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

//                try {
//
//                    Date date = sdf.parse(deadline);
//                    if (date != null) {
//                        long triggerTime = date.getTime();
//
//                        //long triggerTime = System.currentTimeMillis() + 10000; //Testing Purpose
//
//                        Intent intent = new Intent(view.getContext(), ReminderNotification.class);
//                        intent.putExtra("taskName", taskName);
//
//                        PendingIntent pendingIntent = PendingIntent.getBroadcast(
//                                view.getContext(),
//                                (int) System.currentTimeMillis(),
//                                intent,
//                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//                        );
//
//                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

            } else {
                Toast.makeText(view.getContext(), "Task name cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });


        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();


    }


    public void updateTaskStatus(int taskId, int newStatus) {
        DatabaseTask dbHelper = new DatabaseTask(view.getContext());
        dbHelper.updateTaskStatus(taskId, newStatus);
        updateTaskList(dbHelper, 0);
    }

    public void editTaskDialog(Task task) {
        DatabaseTask dbHelper = new DatabaseTask(view.getContext());

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Edit Task");

        LinearLayout layout = new LinearLayout(view.getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText taskInput = new EditText(view.getContext());
        taskInput.setHint("Task Name");
        taskInput.setText(task.getName());
        layout.addView(taskInput);

        final EditText deadlineInput = new EditText(view.getContext());
        deadlineInput.setHint("Deadline (yyyy-MM-dd)");
        deadlineInput.setText(task.getDeadline());
        layout.addView(deadlineInput);

        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String updatedName = taskInput.getText().toString();
            String updatedDeadline = deadlineInput.getText().toString();
            dbHelper.updateTask(task.getId(), updatedName, updatedDeadline);
            updateTaskList(dbHelper, 0);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}