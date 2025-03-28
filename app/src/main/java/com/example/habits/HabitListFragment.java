package com.example.habits;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class HabitListFragment extends Fragment {
    private RecyclerView recyclerView;
    private TaskAdapter adapter;

    // ProgressBar progressBarTasks;
    // TextView progressLabel;

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
        return inflater.inflate(R.layout.fragment_habit_list, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // initialize variables
        recyclerView = view.findViewById(R.id.recyclerViewTasks);

        // initialize database helper
        DatabaseTask dbHelper = new DatabaseTask(view.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.close();

        // initialize fab
        ExtendedFloatingActionButton fab = view.findViewById(R.id.addFab);
        fab.setOnClickListener(fabView -> {
            // navigate to edit habit fragment
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.action_habitListFragment_to_habitEditFragment);
        });

        // initialize bottom sheet
        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.standard_bottom_sheet));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        // initialize top bar
        MaterialToolbar topAppBar = view.findViewById(R.id.toolbar);
        topAppBar.setOnMenuItemClickListener(item -> {
            switch (Objects.requireNonNull(item.getTitle()).toString()) {
                case "Filters":
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    return true;
                case "More":
                    return true;
                default:
                    return false;
            }
        });

        // initialize filter chips
        ChipGroup filterChips = view.findViewById(R.id.filterChips);
        filterChips.setSingleSelection(true);

        filterChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip chip = group.findViewById(checkedIds.get(0));

                switch (chip.getText().toString()) {
                    case "Completed":
                        updateTaskList(dbHelper, 1);
                        break;
                    case "Archived":
                        updateTaskList(dbHelper, 2);
                        break;
                    case "Trashed":
                        updateTaskList(dbHelper, 3);
                        break;
                }
            } else {
                updateTaskList(dbHelper, 0);
            }
        });

        updateTaskList(dbHelper, 0);

        //     progressBarTasks = findViewById(R.id.progressBarTasks);
        //     progressLabel = findViewById(R.id.progressLabel);

        Cursor cursor = dbHelper.getAllTasks();
        while (cursor.moveToNext()) {
            Log.d("Task", "ID: " + cursor.getInt(0) + " Name: " + cursor.getString(1));
        }
        cursor.close();
    }

    private void updateTaskList(DatabaseTask dbHelper, int status) {
        List<Task> taskList = new ArrayList<>();

        // menuTitle.setVisibility(View.VISIBLE);
        // progressBarTasks.setVisibility(View.VISIBLE);
        // progressLabel.setVisibility(View.VISIBLE);

        Cursor cursor;
        switch (status) {
            case 1:
                cursor = dbHelper.getCompletedTasks();
                break;
            case 2:
                cursor = dbHelper.getArchivedTasks();
                break;
            case 3:
                cursor = dbHelper.getTrashedTasks();
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
        }
        cursor.close();

        if (adapter != null && recyclerView.getAdapter() != null) {
            // update the adapter
            adapter.updateTaskList(taskList);
        } else {
            // create the adapter if it doesn't exist
            adapter = new TaskAdapter(getContext(), taskList);

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private void showAddTaskDialog(DatabaseTask dbHelper) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add New Task");

        EditText taskInput = new EditText(getContext());
        taskInput.setHint("Enter Task Name");
        taskInput.setInputType(InputType.TYPE_CLASS_TEXT);

        EditText deadlineInput = new EditText(getContext());
        deadlineInput.setHint("Enter Date & Time (YYYY-MM-DD hh:mm)");
        deadlineInput.setInputType(InputType.TYPE_CLASS_TEXT);
        deadlineInput.setFocusable(false);
        deadlineInput.setClickable(true);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(taskInput);
        layout.addView(deadlineInput);

        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String taskName = taskInput.getText().toString().trim();
            String deadline = deadlineInput.getText().toString().trim();

            Log.d("Task", "Task added: " + taskName);

            if (deadline.isEmpty()) {
                Toast.makeText(getContext(), "Please select a valid deadline!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!taskName.isEmpty()) {
                dbHelper.addTask(taskName, deadline);

                Log.d("Task", "Task added: " + taskName);

                Toast.makeText(getContext(), "Task Added!", Toast.LENGTH_SHORT).show();
                updateTaskList(dbHelper, 0);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

//                try {
//
                // Date date = sdf.parse(deadline);
                // if (date != null) {
                //     long triggerTime = date.getTime();

                //     //long triggerTime = System.currentTimeMillis() + 10000; //Testing Purpose

                //     Intent intent = new Intent(this, ReminderNotification.class);
                //     intent.putExtra("taskName", taskName);

                //     PendingIntent pendingIntent = PendingIntent.getBroadcast(
                //             this,
                //             (int) System.currentTimeMillis(),
                //             intent,
                //             PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                //     );

                //     AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                //     alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                // }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

            } else {
                Toast.makeText(getContext(), "Task name cannot be empty!", Toast.LENGTH_SHORT).show();
            }
        });

        // deadlineInput.setOnClickListener(v -> {
        //     final Calendar calendar = Calendar.getInstance();

        //     DatePickerDialog datePicker = new DatePickerDialog(this,
        //             (view, year, month, dayOfMonth) -> {
        //                 calendar.set(Calendar.YEAR, year);
        //                 calendar.set(Calendar.MONTH, month);
        //                 calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        //                 TimePickerDialog timePicker = new TimePickerDialog(this,
        //                         (timeView, hourOfDay, minute) -> {
        //                             calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        //                             calendar.set(Calendar.MINUTE, minute);

        //                             if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
        //                                 Toast.makeText(this, "⛔ Cannot select a past time!", Toast.LENGTH_SHORT).show();
        //                                 return;
        //                             }

        //                             SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        //                             deadlineInput.setText(sdf.format(calendar.getTime()));
        //                         },
        //                         24, 0, true // Default 00:00 AM
        //                 );

        //                 if (isToday(calendar)) {
        //                     Calendar now = Calendar.getInstance();
        //                     timePicker.updateTime(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
        //                 }

        //                 timePicker.show();
        //             },
        //             calendar.get(Calendar.YEAR),
        //             calendar.get(Calendar.MONTH),
        //             calendar.get(Calendar.DAY_OF_MONTH)
        //     );
        //     datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
        //     datePicker.show();

        // });


        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();


    }


    public void updateTaskStatus(int taskId, int newStatus) {
        DatabaseTask dbHelper = new DatabaseTask(getContext());
        dbHelper.updateTaskStatus(taskId, newStatus);
        updateTaskList(dbHelper, 0);

//                 DatabaseTask dbHelper = new DatabaseTask(this);
//         dbHelper.updateTaskStatus(taskId, newStatus);
//         displayTasks(dbHelper);

// //       Chip chipCompleted = findViewById(R.id.chip2);
// //       Chip chipArchived = findViewById(R.id.chip);
// //       Chip chipTrash = findViewById(R.id.chipTrash);
// //
// //        if (chipCompleted.isChecked()) {
// //            displayFilteredTasks(dbHelper, 1);
// //        } else if (chipArchived.isChecked()) {
// //            displayFilteredTasks(dbHelper, 2);
// //        } else if (chipTrash.isChecked()) {
// //            displayFilteredTasks(dbHelper, 3);
// //        } else {
// //            displayTasks(dbHelper);
// //        }

//         if (newStatus == 1 || newStatus == 2 || newStatus == 3) {
//             Intent intent = new Intent(this, ReminderNotification.class);
//             PendingIntent pendingIntent = PendingIntent.getBroadcast(
//                     this,
//                     taskId,
//                     intent,
//                     PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//             );
//             AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//             if (alarmManager != null) {
//                 alarmManager.cancel(pendingIntent);
//             }
//         }
//         displayTasks(dbHelper);
    }

    // public void editTaskDialog(Task task) {
    //     DatabaseTask dbHelper = new DatabaseTask(this);

    //     AlertDialog.Builder builder = new AlertDialog.Builder(this);
    //     builder.setTitle("Edit Task");

    //     LinearLayout layout = new LinearLayout(this);
    //     layout.setOrientation(LinearLayout.VERTICAL);

    //     final EditText taskInput = new EditText(this);
    //     taskInput.setHint("Task Name");
    //     taskInput.setText(task.getName());
    //     layout.addView(taskInput);

    //     EditText deadlineInput = new EditText(this); // not deadLineInput
    //     deadlineInput.setHint("Select Date & Time");
    //     deadlineInput.setFocusable(false);
    //     deadlineInput.setClickable(true);
    //     layout.addView(deadlineInput);

    //     builder.setView(layout);

    //     deadlineInput.setOnClickListener(v -> {
    //         final Calendar calendar = Calendar.getInstance();

    //         DatePickerDialog datePicker = new DatePickerDialog(this,
    //                 (view, year, month, dayOfMonth) -> {
    //                     calendar.set(Calendar.YEAR, year);
    //                     calendar.set(Calendar.MONTH, month);
    //                     calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

    //                     TimePickerDialog timePicker = new TimePickerDialog(this,
    //                             (timeView, hourOfDay, minute) -> {
    //                                 calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
    //                                 calendar.set(Calendar.MINUTE, minute);

    //                                 if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
    //                                     Toast.makeText(this, "⛔ Cannot select a past time!", Toast.LENGTH_SHORT).show();
    //                                     return;
    //                                 }

    //                                 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    //                                 deadlineInput.setText(sdf.format(calendar.getTime()));
    //                             },
    //                             9, 0, true // Default time: 9:00 AM
    //                     );

    //                     if (isToday(calendar)) {
    //                         Calendar now = Calendar.getInstance();
    //                         timePicker.updateTime(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
    //                     }

    //                     timePicker.show();
    //                 },
    //                 calendar.get(Calendar.YEAR),
    //                 calendar.get(Calendar.MONTH),
    //                 calendar.get(Calendar.DAY_OF_MONTH)
    //         );

    //         datePicker.getDatePicker().setMinDate(System.currentTimeMillis());
    //         datePicker.show();
    //     });


    //     builder.setPositiveButton("Update", (dialog, which) -> {
    //         String updatedName = taskInput.getText().toString();
    //         String updatedDeadline = deadlineInput.getText().toString();
    //         if (updatedDeadline.isEmpty()) {
    //             Toast.makeText(this, "Please select a valid deadline!", Toast.LENGTH_SHORT).show();
    //             return;
    //         }

    //         dbHelper.updateTask(task.getId(), updatedName, updatedDeadline);
    //         displayTasks(dbHelper);

    //         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    //         try {
    //             Date date = sdf.parse(updatedDeadline);
    //             if (date != null) {
    //                 long triggerTime = date.getTime();

    //                 Intent intent = new Intent(this, ReminderNotification.class);
    //                 intent.putExtra("taskName", updatedName);

    //                 PendingIntent pendingIntent = PendingIntent.getBroadcast(
    //                         this,
    //                         task.getId(), // same ID ensures replacement
    //                         intent,
    //                         PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
    //                 );

    //                 AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    //                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    //                     if (alarmManager.canScheduleExactAlarms()) {
    //                         alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    //                     } else {
    //                         Toast.makeText(this, "Exact alarms not permitted. Please allow in settings.", Toast.LENGTH_LONG).show();
    //                     }
    //                 }
    //             }
    //         } catch (Exception e) {
    //             e.printStackTrace();
    //         }
    //     });
    //     builder.setNegativeButton("Cancel", null);
    //     builder.show();
//    }

    //     private void displayTasks(DatabaseTask dbHelper) {
    //     taskList.clear();
    //     Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM tasks WHERE status = 0", null);

    //     while (cursor.moveToNext()) {
    //         int id = cursor.getInt(0);
    //         String name = cursor.getString(1);
    //         String deadline = cursor.getString(2);
    //         int taskStatus = cursor.getInt(3);
    //         taskList.add(new Task(id, name, deadline, taskStatus));
    //     }

    //     cursor.close();

    //     int todayTotal = 0;
    //     int todayCompleted = 0;
    //     String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

    //     for (Task task : taskList) {
    //         String deadline = task.getDeadline();
    //         if (deadline != null && deadline.length() >= 10) {
    //             String taskDate = deadline.substring(0, 10);
    //             if (taskDate.equals(today)) {
    //                 todayTotal++;
    //                 if (task.getStatus() == 1) {
    //                     todayCompleted++;
    //                 }
    //             }
    //         }
    //     }

    //     if (todayTotal > 0) {
    //         progressBarTasks.setVisibility(View.VISIBLE);
    //         progressLabel.setVisibility(View.VISIBLE);
    //         int percent = (int) (((double) todayCompleted / todayTotal) * 100);
    //         progressBarTasks.setProgress(percent);
    //         progressLabel.setText("📊 Today's Progress: " + todayCompleted + " / " + todayTotal);
    //     } else {
    //         progressBarTasks.setVisibility(View.GONE);
    //         progressLabel.setVisibility(View.GONE);
    //     }

    //     if (adapter == null) {
    //         adapter = new TaskAdapter(this, taskList);
    //         recyclerView.setLayoutManager(new LinearLayoutManager(this));
    //         recyclerView.setAdapter(adapter);
    //     } else {
    //         adapter.notifyDataSetChanged();
    //     }
    // }


//    private boolean selectedDateIsToday(Calendar calendar) {
//        Calendar today = Calendar.getInstance();
//        return today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
//                && today.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR);
//    }
//
//
//    private boolean isToday(Calendar calendar) {
//        Calendar today = Calendar.getInstance();
//        return today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
//                today.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR);
//    }
//
//    private void updateProgressBar(DatabaseTask dbHelper) {
//        Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM tasks WHERE status = 0 OR status = 1", null);
//        int todayTotal = 0;
//        int todayCompleted = 0;
//        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
//
//        while (cursor.moveToNext()) {
//            String deadline = cursor.getString(2);
//            int status = cursor.getInt(3);
//            if (deadline != null && deadline.length() >= 10) {
//                String taskDate = deadline.substring(0, 10);
//                if (taskDate.equals(today)) {
//                    todayTotal++;
//                    if (status == 1) todayCompleted++;
//                }
//            }
//        }
//        cursor.close();
//
//        if (todayTotal > 0) {
//            int percent = (int) (((double) todayCompleted / todayTotal) * 100);
//            progressBarTasks.setProgress(percent);
//            progressLabel.setText("📊 Today's Progress: " + todayCompleted + " / " + todayTotal);
//            progressBarTasks.setVisibility(View.VISIBLE);
//            progressLabel.setVisibility(View.VISIBLE);
//        } else {
//            progressBarTasks.setVisibility(View.GONE);
//            progressLabel.setVisibility(View.GONE);
//        }
//    }
}