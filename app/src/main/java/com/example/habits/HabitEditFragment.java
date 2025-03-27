package com.example.habits;

import static android.text.format.DateUtils.isToday;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HabitEditFragment extends Fragment {
    private TaskAdapter adapter;

    public HabitEditFragment() {
        // Required empty public constructor
    }

    public static HabitEditFragment newInstance() {
        HabitEditFragment fragment = new HabitEditFragment();
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
        View view = inflater.inflate(R.layout.fragment_habit_edit, container, false);

        // return the inflated view
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText titleEditText = view.findViewById(R.id.titleEditText);
        TextInputEditText dateEditText = view.findViewById(R.id.dateEditText);

        // set up the date picker
        dateEditText.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePicker = new DatePickerDialog(v.getContext(),
                    (datePickerView, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        TimePickerDialog timePicker = new TimePickerDialog(v.getContext(),
                                (timePickerView, hourOfDay, minute) -> {
                                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    calendar.set(Calendar.MINUTE, minute);

                                    if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                                        Toast.makeText(v.getContext(), "⛔ Cannot select a past time!", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                                    dateEditText.setText(sdf.format(calendar.getTime()));
                                },
                                9, 0, true // Default time: 9:00 AM
                        );

                        if (isToday(calendar.getTimeInMillis())) {
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

        // set up the submit button
        Button submitButton = view.findViewById(R.id.submitButton);

        submitButton.setOnClickListener(v -> {
            String name = titleEditText.getText().toString().trim();
            String deadline = dateEditText.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(v.getContext(), "⛔ Please enter a habit name!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (deadline.isEmpty()) {
                Toast.makeText(v.getContext(), "⛔ Please select a deadline!", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseTask dbHelper = new DatabaseTask(v.getContext());
            dbHelper.addTask(name, deadline);

            // navigate back to the habit list fragment
            getParentFragmentManager().popBackStack();
        });

        // set up the navigation button
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            // navigate back to the habit list fragment
            getParentFragmentManager().popBackStack();
        });
    }
}