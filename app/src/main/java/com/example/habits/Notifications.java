package com.example.habits;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

public class Notifications extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        String taskTitle = intent.getStringExtra("taskTitle");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "task_reminder")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Task Reminder")
                .setContentText("Reminder: " + taskTitle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

}
