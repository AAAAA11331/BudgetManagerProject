package com.example.budgetmanager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import com.example.budgetmanager.R;

public class MainDashboardActivity extends BaseActivity implements TransactionAdapter.OnTransactionChangeListener {
    private TextView tvBalance, tvGreeting, tvEmptyState;
    private RecyclerView rvRecentTransactions;
    private DatabaseHelper dbHelper;
    private TransactionAdapter recentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_dashboard);

        dbHelper = new DatabaseHelper(this);
        tvGreeting = findViewById(R.id.tvGreeting);
        tvBalance = findViewById(R.id.tvBalance);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        rvRecentTransactions = findViewById(R.id.rvRecentTransactions);

        String username = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("username", "User");
        tvGreeting.setText("欢迎回来，" + username + "!");

        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(this));
        recentAdapter = new TransactionAdapter(new ArrayList<>(), this);
        rvRecentTransactions.setAdapter(recentAdapter);

        FloatingActionButton fab = findViewById(R.id.fab_add_transaction);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddTransactionActivity.class)));

        setupBottomNavigation();
        updateDashboard();
    }

    @Override
    protected int getSelectedItemId() {
        return R.id.nav_dashboard;
    }

    @Override
    protected Class<?> getActivityClass() {
        return MainDashboardActivity.class;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDashboard();
    }

    private void updateDashboard() {
        List<Transaction> recentTx = loadRecentTransactions();
        recentAdapter.updateTransactions(recentTx);

        if (recentTx.isEmpty()) {
            rvRecentTransactions.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvRecentTransactions.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }

        double balance = calculateCurrentBalance();
        tvBalance.setText(String.format("当前余额: $%.2f", balance));
        tvBalance.setTextColor(balance >= 0 ? Color.parseColor("#2E7D32") : Color.parseColor("#C62828"));
    }

    private List<Transaction> loadRecentTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query("transactions", new String[]{"id", "type", "category", "amount", "date"},
                null, null, null, null, "id DESC", "3")) {
            while (cursor.moveToNext()) {
                transactions.add(new Transaction(
                        cursor.getLong(0),
                        cursor.getString(1) != null ? cursor.getString(1) : "Unknown",
                        cursor.getString(2) != null ? cursor.getString(2) : "Unknown",
                        cursor.getDouble(3),
                        cursor.getString(4) != null ? cursor.getString(4) : "N/A"
                ));
            }
        } finally {
            db.close();
        }
        return transactions;
    }

    private double calculateCurrentBalance() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double balance = 0;
        try (Cursor cursor = db.rawQuery("SELECT SUM(CASE WHEN type='Income' THEN amount ELSE -amount END) as balance FROM transactions", null)) {
            if (cursor.moveToFirst()) {
                balance = cursor.getDouble(0);
            }
        } finally {
            db.close();
        }
        return balance;
    }

    @Override
    public void onTransactionDeleted() {
        updateDashboard();
    }
}