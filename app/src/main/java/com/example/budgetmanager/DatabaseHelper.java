package com.example.budgetmanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;
import java.io.FileWriter;
import java.io.IOException;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "budget_app.db";
    private static final int DB_VERSION = 6;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create transactions table
        db.execSQL("CREATE TABLE transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "type TEXT NOT NULL, " +
                "category TEXT NOT NULL, " +
                "amount REAL NOT NULL, " +
                "date TEXT NOT NULL, " +
                "note TEXT, " +
                "frequency TEXT DEFAULT 'One-time', " +
                "frequency_value TEXT, " +
                "frequency_unit TEXT)");

        // Create users table with UNIQUE constraint on username
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL)");

        // Create notifications table with foreign key
        db.execSQL("CREATE TABLE notifications (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "monthly_report INTEGER DEFAULT 0, " +
                "budget_warning INTEGER DEFAULT 0, " +
                "FOREIGN KEY(username) REFERENCES users(username))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Incremental upgrades for schema changes
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE transactions ADD COLUMN frequency TEXT DEFAULT 'One-time'");
            db.execSQL("ALTER TABLE transactions ADD COLUMN frequency_value TEXT");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE transactions ADD COLUMN amount REAL NOT NULL DEFAULT 0");
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE transactions ADD COLUMN frequency_unit TEXT");
        }
        if (oldVersion < 5) {
            db.execSQL("CREATE TABLE users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL)");
        }
        if (oldVersion < 6) {
            db.execSQL("CREATE TABLE notifications (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "monthly_report INTEGER DEFAULT 0, " +
                    "budget_warning INTEGER DEFAULT 0, " +
                    "FOREIGN KEY(username) REFERENCES users(username))");
        }
    }

    /**
     * Register a new user and set default notifications.
     * Returns false if username already exists.
     */
    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if username already exists
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE username = ?", new String[]{username});
        if (cursor.moveToFirst()) {
            cursor.close();
            db.close();
            return false; // Username exists, registration fails
        }
        cursor.close();

        // Insert new user
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", SecurityUtils.hashPassword(password));
        long result = db.insert("users", null, values);

        // If user is successfully added, set default notifications
        if (result != -1) {
            ContentValues notificationValues = new ContentValues();
            notificationValues.put("username", username);
            notificationValues.put("monthly_report", 1); // Default: enabled
            notificationValues.put("budget_warning", 0); // Default: disabled
            db.insert("notifications", null, notificationValues);
        }
        db.close();
        return result != -1; // Return true if insertion succeeds
    }

    /**
     * Authenticate a user by comparing hashed password.
     */
    public boolean authenticateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT password FROM users WHERE username = ?",
                new String[]{username});
        boolean isAuthenticated = false;
        if (cursor.moveToFirst()) {
            String storedHash = cursor.getString(0);
            isAuthenticated = SecurityUtils.hashPassword(password).equals(storedHash);
        }
        cursor.close();
        db.close();
        return isAuthenticated;
    }

    /**
     * Get notification preference for a user (monthly_report or budget_warning).
     */
    public boolean getNotificationPreference(String username, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + type + " FROM notifications WHERE username = ?",
                new String[]{username});
        boolean enabled = false;
        if (cursor.moveToFirst()) {
            enabled = cursor.getInt(0) == 1;
        }
        cursor.close();
        db.close();
        return enabled;
    }

    /**
     * Update or insert notification preference for a user.
     */
    public void updateNotificationPreference(String username, String type, boolean isEnabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(type, isEnabled ? 1 : 0);
        int rows = db.update("notifications", values, "username = ?", new String[]{username});
        if (rows == 0) {
            values.put("username", username);
            db.insert("notifications", null, values);
        }
        db.close();
    }

    /**
     * Export transactions table to a CSV file.
     */
    public void exportDatabaseToCSV(Context context, String filePath) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT * FROM transactions", null);
             FileWriter writer = new FileWriter(filePath)) {
            writer.append("id,type,category,amount,date,note,frequency,frequency_value,frequency_unit\n");
            while (cursor.moveToNext()) {
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    writer.append(cursor.isNull(i) ? "" : cursor.getString(i));
                    if (i < cursor.getColumnCount() - 1) writer.append(",");
                }
                writer.append("\n");
            }
            Toast.makeText(context, "Exported to " + filePath, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }
}