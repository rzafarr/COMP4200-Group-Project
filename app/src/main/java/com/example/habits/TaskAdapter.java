package com.example.habits;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;

    private List<Task> tasks;

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
        holder.taskName.setText("📝 " + task.getName());
        holder.taskDeadline.setText(task.getDeadline());

        holder.completeButton.setVisibility(View.GONE);
        holder.editButton.setVisibility(View.GONE);
        holder.deleteButton.setVisibility(View.GONE);
        holder.archiveButton.setVisibility(View.GONE);
        holder.restoreButton.setVisibility(View.GONE);

        switch (task.getStatus()) {
            case 0:
                // task is not completed, archived, or trashed
                holder.completeButton.setVisibility(View.VISIBLE);
                holder.editButton.setVisibility(View.VISIBLE);
                holder.deleteButton.setVisibility(View.VISIBLE);
                holder.archiveButton.setVisibility(View.VISIBLE);
                break;
            case 1:
                // task is completed
                holder.restoreButton.setVisibility(View.VISIBLE);
                break;
            case 2:
                // task is archived
                holder.restoreButton.setVisibility(View.VISIBLE);
                break;
            case 3:
                // task is trashed
                holder.restoreButton.setVisibility(View.VISIBLE);
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
