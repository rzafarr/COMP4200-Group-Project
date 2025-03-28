package com.example.habits;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;

    private DatabaseTask dbHelper;
    private Context context;
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
                    dbTask.updateTaskStatus(task.getId(), 1);
                    taskList.remove(position);
                    notifyItemRemoved(position);

                    Snackbar snackbar = Snackbar.make(v, "Task '" + task.getName() + "' has been deleted", Snackbar.LENGTH_LONG);
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
                    FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.replace(R.id.nav_host_fragment, habitEditFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                });

                holder.deleteButton.setOnClickListener(v -> {
                    dbTask.updateTaskStatus(task.getId(), 3);
                    taskList.remove(position);
                    notifyItemRemoved(position);

                    Snackbar snackbar = Snackbar.make(v, "Task '" + task.getName() + "' has been deleted", Snackbar.LENGTH_LONG);
                    snackbar.show();
                });

                holder.archiveButton.setOnClickListener(v -> {
                    dbTask.updateTaskStatus(task.getId(), 2);
                    taskList.remove(position);
                    notifyItemRemoved(position);

                    Snackbar snackbar = Snackbar.make(v, "Task '" + task.getName() + "' has been deleted", Snackbar.LENGTH_LONG);
                    snackbar.show();
                });
                break;
            case 1:
            case 2:
            case 3:
                // task is completed, archived, or trashed
                holder.restoreButton.setVisibility(View.VISIBLE);

                holder.restoreButton.setOnClickListener(v -> {
                    dbTask.updateTaskStatus(task.getId(), 0);
                    taskList.remove(position);
                    notifyItemRemoved(position);

                    Snackbar snackbar = Snackbar.make(v, "Task '" + task.getName() + "' has been restored", Snackbar.LENGTH_LONG);
                    snackbar.show();
                });
                break;
        }

//        if (task.getStatus() == 0) {
//            holder.itemView.setOnLongClickListener(v -> {
//                Task taskItem = taskList.get(holder.getAdapterPosition());
//
//                 String[] options = {"✏️ Edit", "✅ Mark as Completed", "📦 Archive", "🗑️ Delete"};
//
//                 new AlertDialog.Builder(context)
//                         .setTitle("Update Task")
//                         .setItems(options, (dialog, which) -> {
//                             switch (which) {
//                                 case 0:
//                                     if (context instanceof MainActivity) {
//                                         ((MainActivity) context).editTaskDialog(taskItem);
//                                     }
//                                     break;
//                                 case 1:
//                                     if (context instanceof MainActivity) {
//                                         ((MainActivity) context).updateTaskStatus(taskItem.getId(), 1);
//                                     }
//                                     break;
//                                 case 2:
//                                     if (context instanceof MainActivity) {
//                                         ((MainActivity) context).updateTaskStatus(taskItem.getId(), 2);
//                                     }
//                                     break;
//                                 case 3:
//                                     if (context instanceof MainActivity) {
//                                         ((MainActivity) context).updateTaskStatus(taskItem.getId(), 3);
//                                     }
//                                     break;
//                             }
//
//                         })
//
//                         .setCancelable(true)
//                         .show();
//
//                 return true;
//             });
//         } else {
//             holder.itemView.setOnLongClickListener(v -> {
//                 Task taskItem = taskList.get(holder.getAdapterPosition());
//                 String title = "Manage Task";
//                 String message = "What would you like to do?";
//                 String moveTo = "🔄 Move to Active";
//
//                 if (taskItem.getStatus() == 1) {
//                     message = "Task marked as Completed.";
//                     moveTo = "Mark as In Progress";
//                 } else if (taskItem.getStatus() == 2) {
//                     message = "Task is Archived.";
//                     moveTo = "🔄 Unarchive";
//                 }
//
//                 new AlertDialog.Builder(v.getContext())
//                         .setTitle(title)
//                         .setMessage(message)
//                         .setPositiveButton(moveTo, (dialog, which) -> {
//                             if (v.getContext() instanceof MainActivity) {
//                                 ((MainActivity) v.getContext()).updateTaskStatus(taskItem.getId(), 0);
//                             }
//                         })
//                         .setNegativeButton("🗑️ Delete", (dialog, which) -> {
//                             if (v.getContext() instanceof MainActivity) {
//                                 ((MainActivity) v.getContext()).updateTaskStatus(taskItem.getId(), 3);
//                             }
//                         })
//                         .show();
//
//                return true;
//            });
//        }
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
        dbHelper.updateTaskStatus(taskId, newStatus);
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
