package com.example.habits;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseTask extends SQLiteOpenHelper{
    private static final String TAG = "DatabaseTask";
    private static final String DATABASE_NAME = "tasks.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseTask(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
       // Log.d(TAG, "Creating database...");
        db.execSQL("CREATE TABLE tasks ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "deadline TEXT, "
                + "status INTEGER NOT NULL DEFAULT 0);");
       // Log.d(TAG, "Database created successfully.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      //  Log.d(TAG, "Upgrading database...");
        db.execSQL("DROP TABLE IF EXISTS tasks");
        onCreate(db);
      //  Log.d(TAG, "Database upgraded.");
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

    public void updateTaskStatus(int taskId, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        db.update("tasks", values, "_id=?", new String[]{String.valueOf(taskId)});
        db.close();
    }

    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("tasks", "_id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void updateTask(int id, String name, String deadline) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("deadline", deadline);

        db.update("tasks", values, "_id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public int getLastInsertedTaskId() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(id) FROM tasks", null);
        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        cursor.close();
        return id;
    }


}
