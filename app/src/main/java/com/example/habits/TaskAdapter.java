package com.example.habits;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;

    private final DatabaseTask dbHelper;
    private final Context context;

    public TaskAdapter(Context context, List<Task> taskList) {
        this.taskList = taskList;
        this.dbHelper = new DatabaseTask(context);
        this.context = context;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.taskName.setText(task.getName());
        holder.taskDeadline.setText(task.getDeadline());

        holder.completeButton.setVisibility(View.GONE);
        holder.editButton.setVisibility(View.GONE);
        holder.deleteButton.setVisibility(View.GONE);
        holder.archiveButton.setVisibility(View.GONE);
        holder.restoreButton.setVisibility(View.GONE);

        DatabaseTask dbTask = new DatabaseTask(context);

        switch (task.getStatus()) {
            case 0:
                // task is not completed, archived, or trashed
                holder.completeButton.setVisibility(View.VISIBLE);
                holder.editButton.setVisibility(View.VISIBLE);
                holder.deleteButton.setVisibility(View.VISIBLE);
                holder.archiveButton.setVisibility(View.VISIBLE);

                holder.completeButton.setOnClickListener(v -> {
                    dbTask.updateTaskStatus(task.getId(), 1, v.getContext());
                    taskList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, getItemCount());

                    Snackbar snackbar = Snackbar.make(v, "Task '" + task.getName() + "' has been completed.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                });

                holder.editButton.setOnClickListener(v -> {
                    // set up bundle params
                    Bundle bundle = new Bundle();
                    bundle.putInt("taskId", task.getId());
                    bundle.putString("taskName", task.getName());
                    bundle.putString("taskDeadline", task.getDeadline());

                    // pass bundle to HabitEditFragment
                    HabitEditFragment habitEditFragment = new HabitEditFragment();
                    habitEditFragment.setArguments(bundle);

                    // go to HabitEditFragment
                    FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.nav_host_fragment, habitEditFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                });

                holder.deleteButton.setOnClickListener(v -> {
                    dbTask.updateTaskStatus(task.getId(), 3, v.getContext());
                    taskList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, getItemCount());

                    Snackbar snackbar = Snackbar.make(v, "Task '" + task.getName() + "' has been deleted.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                });

                holder.archiveButton.setOnClickListener(v -> {
                    dbTask.updateTaskStatus(task.getId(), 2, v.getContext());
                    taskList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, getItemCount());

                    Snackbar snackbar = Snackbar.make(v, "Task '" + task.getName() + "' has been archived.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                });
                break;
            case 1:
            case 2:
            case 3:
                // task is completed, archived, or trashed
                holder.restoreButton.setVisibility(View.VISIBLE);

                holder.restoreButton.setOnClickListener(v -> {
                    dbTask.updateTaskStatus(task.getId(), 0, v.getContext());
                    taskList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, getItemCount());

                    Snackbar snackbar = Snackbar.make(v, "Task '" + task.getName() + "' has been restored", Snackbar.LENGTH_LONG);
                    snackbar.show();
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    private void updateTaskName(int taskId, String newName) {
        taskList.get(taskId).setName(newName);
        dbHelper.updateTaskName(taskId, newName);
        notifyItemChanged(taskId);
    }

    private void updateTaskDeadline(int taskId, String newDeadline) {
        taskList.get(taskId).setDeadline(newDeadline);
        dbHelper.updateTaskDeadline(taskId, newDeadline);
        notifyItemChanged(taskId);
    }

    private void updateTaskStatus(int taskId, int newStatus) {
        taskList.get(taskId).setStatus(newStatus);
        dbHelper.updateTaskStatus(taskId, newStatus, context);
        notifyItemRemoved(taskId);
    }

    public void updateTaskList(List<Task> newTaskList) {
        taskList = newTaskList;
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskName, taskDeadline;
        Button completeButton, editButton, deleteButton, archiveButton, restoreButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.textTaskName);
            taskDeadline = itemView.findViewById(R.id.textTaskDeadline);

            completeButton = itemView.findViewById(R.id.completeButton);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            archiveButton = itemView.findViewById(R.id.archiveButton);
            restoreButton = itemView.findViewById(R.id.restoreButton);
        }
    }
}
