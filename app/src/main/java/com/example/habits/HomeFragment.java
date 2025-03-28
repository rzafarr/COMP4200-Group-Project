package com.example.habits;

import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {
    public HomeFragment() {
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
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView taskProgressText = view.findViewById(R.id.taskProgressText);
        LinearProgressIndicator taskProgressIndicator = view.findViewById(R.id.taskProgressIndicator);

        int activeToday = 0;
        int completedToday = 0;

        DatabaseTask dbHelper = new DatabaseTask(view.getContext());
        Cursor cursor = dbHelper.getAllTasks();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        while (cursor.moveToNext()) {
            String taskDate = cursor.getString(2).substring(0, 10);
            if (taskDate.equals(today)) {
                activeToday++;
            }
        }

        cursor = dbHelper.getCompletedTasks();
        while (cursor.moveToNext()) {
            String taskDate = cursor.getString(2).substring(0, 10);
            if (taskDate.equals(today)) {
                completedToday++;
            }
        }

        cursor.close();

        // three states
        // 1. no tasks in total
        // 2. some tasks in total, less than that many completed
        // 3. no active tasks and some nonzero amount of completed tasks

        if (activeToday == 0 && completedToday == 0) {
            taskProgressText.setText("You don't have any tasks for today. Try adding some to see your progress here!");
            taskProgressIndicator.setProgress(0);
        }
        else if (activeToday != 0 && completedToday < activeToday) {
            int progress = (completedToday * 100) / (activeToday + completedToday);
            taskProgressText.setText("You've completed " + completedToday + " out of " + (activeToday + completedToday) + " tasks today! Keep at it!");
            taskProgressIndicator.setProgress(progress);
        }
        else if (activeToday == 0 && completedToday != 0) {
            int progress = (completedToday * 100) / (activeToday + completedToday);
            taskProgressText.setText("You've completed all " + completedToday + " task(s) today! 🎇");
            taskProgressIndicator.setProgress(progress);
        }
    }
}