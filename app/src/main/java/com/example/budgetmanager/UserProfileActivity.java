package com.example.budgetmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.example.budgetmanager.R;

public class UserProfileActivity extends BaseActivity {
    private TextView tvUsername;
    private CheckBox checkboxMonthlyReport, checkboxBudgetWarning;
    private Button btnSave, btnSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        tvUsername = findViewById(R.id.tvUsername);
        checkboxMonthlyReport = findViewById(R.id.checkboxMonthlyReport);
        checkboxBudgetWarning = findViewById(R.id.checkboxBudgetWarning);
        btnSave = findViewById(R.id.btnSave);
        btnSignOut = findViewById(R.id.btnSignOut);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", null);
        if (username != null) {
            tvUsername.setText("用户名: " + username);
            checkboxMonthlyReport.setChecked(dbHelper.getNotificationPreference(username, "monthly_report"));
            checkboxBudgetWarning.setChecked(dbHelper.getNotificationPreference(username, "budget_warning"));
        } else {
            Toast.makeText(this, "未登录用户", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        btnSave.setOnClickListener(v -> {
            dbHelper.updateNotificationPreference(username, "monthly_report", checkboxMonthlyReport.isChecked());
            dbHelper.updateNotificationPreference(username, "budget_warning", checkboxBudgetWarning.isChecked());
            Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show();
        });

        btnSignOut.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        setupBottomNavigation();
    }

    @Override
    protected int getSelectedItemId() {
        return R.id.nav_profile;
    }

    @Override
    protected Class<?> getActivityClass() {
        return UserProfileActivity.class;
    }
}