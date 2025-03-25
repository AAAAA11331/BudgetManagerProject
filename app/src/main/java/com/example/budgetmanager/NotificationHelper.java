package com.example.budgetmanager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class NotificationHelper {
    private static final String CHANNEL_ID = "BudgetManagerChannel";
    private final Context context;
    private final DatabaseHelper dbHelper;

    public NotificationHelper(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        createNotificationChannel();
    }

    public void triggerNotification(String type) {
        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", null);
        if (username != null && dbHelper.getNotificationPreference(username, type)) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(type.equals(NotificationTypes.MONTHLY_REPORT) ? "Monthly Report" : "Budget Warning")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            if (type.equals(NotificationTypes.MONTHLY_REPORT)) {
                double net = calculateNetForMonth();
                builder.setContentText(String.format("Net for this month: $%.2f", net));
            } else {
                builder.setContentText("Check your budget now!");
            }

            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    private double calculateNetForMonth() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double net = 0;
        String currentMonth = String.format("%04d-%02d", Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH) + 1);
        try (Cursor cursor = db.rawQuery("SELECT SUM(CASE WHEN type='Income' THEN amount ELSE -amount END) as net " +
                "FROM transactions WHERE date LIKE ?", new String[]{currentMonth + "%"})) {
            if (cursor.moveToFirst()) {
                net = cursor.getDouble(0);
            }
        } finally {
            db.close();
        }
        return net;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Budget Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}