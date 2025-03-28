package com.example.habits;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseTask extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "tasks.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseTask(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE tasks ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "deadline TEXT, "
                + "status INTEGER NOT NULL DEFAULT 0);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS tasks");
        onCreate(db);
    }

    public void addTask(String name, String deadline) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("deadline", deadline);
        db.insert("tasks", null, values);
        db.close();
    }

    public Cursor getAllTasks() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM tasks WHERE status = 0 OR status = 0", null);
    }

    public Cursor getCompletedTasks() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM tasks WHERE status = 1", null);
    }

    public Cursor getArchivedTasks() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM tasks WHERE status = 2", null);
    }
    public Cursor getTrashedTasks() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM tasks WHERE status = 3", null);
    }

    public void updateTaskStatus(int taskId, int status, Context context) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        db.update("tasks", values, "_id=?", new String[]{String.valueOf(taskId)});
        db.close();

        if (status == 1 || status == 2 || status == 3) {
            // cancel existing alarm
            Intent intent = new Intent(context, ReminderNotification.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, taskId, intent, PendingIntent.FLAG_UPDATE_CURRENT  | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
        }
    }

    public void updateTaskName(int id, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);

        db.update("tasks", values, "_id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void updateTaskDeadline(int id, String deadline) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("deadline", deadline);

        db.update("tasks", values, "_id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public int getLastInsertedTaskId() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(_id) FROM tasks", null);
        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        cursor.close();
        return id;
    }


}
