package com.example.habits;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;

    private List<Task> tasks;

    private DatabaseTask dbHelper;

    public TaskAdapter(Context context, List<Task> taskList) {
        this.taskList = taskList;
        this.dbHelper = new DatabaseTask(context);
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

        if (task.getStatus() == 0) {
            holder.itemView.setOnLongClickListener(v -> {
                Task taskItem = taskList.get(holder.getAdapterPosition());

//                new AlertDialog.Builder(v.getContext())
//                        .setTitle("Update Task")
//                        .setMessage("What would you like to do?")
//                        .setPositiveButton("Mark as Completed", (dialog, which) -> {
//                            if (v.getContext() instanceof MainActivity) {
//                                ((MainActivity) v.getContext()).updateTaskStatus(taskItem.getId(), 1); // Completed
//                            }
//                        })
//                        .setNegativeButton("Delete", (dialog, which) -> {
//                            dbHelper.deleteTask(taskItem.getId());
//                            taskList.remove(holder.getAdapterPosition());
//                            notifyItemRemoved(holder.getAdapterPosition());
//                        })
//                        .setNeutralButton("Archive", (dialog, which) -> {
//                            if (v.getContext() instanceof MainActivity) {
//                                ((MainActivity) v.getContext()).updateTaskStatus(taskItem.getId(), 2); // Archived
//                            }
//                        })
//                        .show();

                return true;
            });
        }else{
            holder.itemView.setOnLongClickListener(null);
        }

    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskName, taskDeadline;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.textTaskName);
            taskDeadline = itemView.findViewById(R.id.textTaskDeadline);
        }
    }
}
