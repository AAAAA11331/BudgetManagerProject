package com.example.budgetmanager;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.util.Log;
import android.widget.Toast;

import com.example.budgetmanager.R;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    protected BottomNavigationView bottomNav;
    protected abstract int getSelectedItemId();
    protected abstract Class<?> getActivityClass();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    protected void setupBottomNavigation() {
        bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav == null) {
            return; // Exit early if navigation view is missing
        }

        bottomNav.setSelectedItemId(getSelectedItemId());
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == getSelectedItemId()) {
                return true;
            }

            Intent intent = null;
            Class<?> targetActivity = null;
            if (itemId == R.id.nav_dashboard) {
                targetActivity = MainDashboardActivity.class;
            } else if (itemId == R.id.nav_transactions) {
                targetActivity = RecentTransactionsActivity.class;
            } else if (itemId == R.id.nav_reports) {
                targetActivity = MonthlyReportActivity.class;
            } else if (itemId == R.id.nav_profile) {
                targetActivity = UserProfileActivity.class;
            }

            if (targetActivity != null) {
                intent = new Intent(this, targetActivity);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                if (!getActivityClass().equals(targetActivity)) {
                    finish();
                }
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(getSelectedItemId());
        }
    }
}