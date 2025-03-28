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

import androidx.annotation.NonNull;
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

    public HabitListFragment() {
        // Required empty public constructor
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

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
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

        Cursor cursor = dbHelper.getAllTasks();
        while (cursor.moveToNext()) {
            Log.d("Task", "ID: " + cursor.getInt(0) + " Name: " + cursor.getString(1));
        }
        cursor.close();
    }

    private void updateTaskList(DatabaseTask dbHelper, int status) {
        List<Task> taskList = new ArrayList<>();

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

        if (adapter.getItemCount() == 0) {
            requireView().findViewById(R.id.emptyMessageText).setVisibility(View.VISIBLE);
        } else {
            requireView().findViewById(R.id.emptyMessageText).setVisibility(View.GONE);
        }
    }
}