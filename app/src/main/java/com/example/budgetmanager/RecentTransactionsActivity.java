package com.example.budgetmanager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.app.DatePickerDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import com.example.budgetmanager.R;

public class RecentTransactionsActivity extends BaseActivity implements TransactionAdapter.OnTransactionChangeListener {
    private static final String TAG = "RecentTransactions";
    private Button buttonApplyFilters;
    private EditText editTextNameFilter, editTextFromDate, editTextToDate;
    private Spinner spinnerCategory;
    private RecyclerView recyclerViewTransactions;
    private TextView tvEmptyState;
    private TransactionAdapter adapter;
    private List<Transaction> allTransactions;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_transactions);
        Log.d(TAG, "onCreate called");

        buttonApplyFilters = findViewById(R.id.button_applyFilters);
        editTextNameFilter = findViewById(R.id.editText_nameFilter);
        editTextFromDate = findViewById(R.id.editText_fromDate);
        editTextToDate = findViewById(R.id.editText_toDate);
        spinnerCategory = findViewById(R.id.spinner_category);
        recyclerViewTransactions = findViewById(R.id.recyclerView_transactions);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        Log.d(TAG, "Views initialized: buttonApplyFilters=" + (buttonApplyFilters != null) +
                ", editTextNameFilter=" + (editTextNameFilter != null) +
                ", tvEmptyState=" + (tvEmptyState != null));

        Button btnThisMonth = findViewById(R.id.btnThisMonth);
        if (btnThisMonth != null) {
            btnThisMonth.setOnClickListener(v -> {
                Calendar cal = Calendar.getInstance();
                editTextFromDate.setText(String.format("%04d-%02d-01", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1));
                editTextToDate.setText(String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.DAY_OF_MONTH)));
                applyFilters();
            });
        }

        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        allTransactions = loadTransactionsFromDB();
        adapter = new TransactionAdapter(allTransactions, this);
        recyclerViewTransactions.setAdapter(adapter);

        if (allTransactions.isEmpty()) {
            recyclerViewTransactions.setVisibility(View.GONE);
            if (tvEmptyState != null) {
                tvEmptyState.setVisibility(View.VISIBLE);
            }
        } else {
            recyclerViewTransactions.setVisibility(View.VISIBLE);
            if (tvEmptyState != null) {
                tvEmptyState.setVisibility(View.GONE);
            }
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"All", "Food", "Transport", "Salary", "Shopping"});
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        editTextFromDate.setOnClickListener(v -> showDatePickerDialog(editTextFromDate));
        editTextToDate.setOnClickListener(v -> showDatePickerDialog(editTextToDate));

        buttonApplyFilters.setOnClickListener(v -> applyFilters());

        setupBottomNavigation();
    }

    @Override
    protected int getSelectedItemId() {
        return R.id.nav_transactions;
    }

    @Override
    protected Class<?> getActivityClass() {
        return RecentTransactionsActivity.class;
    }

    private void showDatePickerDialog(EditText targetEditText) {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, day) -> {
            String dateStr = String.format("%04d-%02d-%02d", year, (month + 1), day);
            targetEditText.setText(dateStr);
        };
        DatePickerDialog dialog = new DatePickerDialog(this, android.R.style.Theme_Material_Light_Dialog, dateSetListener, 2023, 0, 1);
        dialog.show();
    }

    private void applyFilters() {
        String nameFilter = editTextNameFilter.getText().toString().trim();
        String fromDateStr = editTextFromDate.getText().toString().trim();
        String toDateStr = editTextToDate.getText().toString().trim();
        String categoryFilter = spinnerCategory.getSelectedItem().toString();

        long fromMillis = 0, toMillis = Long.MAX_VALUE;
        try {
            if (!fromDateStr.isEmpty()) {
                fromMillis = dateFormat.parse(fromDateStr).getTime();
            }
            if (!toDateStr.isEmpty()) {
                toMillis = dateFormat.parse(toDateStr).getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        List<Transaction> filteredList = new ArrayList<>();
        for (Transaction tx : allTransactions) {
            if (!nameFilter.isEmpty() && !tx.getName().toLowerCase().contains(nameFilter.toLowerCase())) {
                continue;
            }
            if (!categoryFilter.equals("All") && !tx.getCategory().equals(categoryFilter)) {
                continue;
            }
            long txDate = tx.getDateMillis();
            if (txDate < fromMillis || txDate > toMillis) {
                continue;
            }
            filteredList.add(tx);
        }
        adapter.updateTransactions(filteredList);

        if (filteredList.isEmpty()) {
            recyclerViewTransactions.setVisibility(View.GONE);
            if (tvEmptyState != null) {
                tvEmptyState.setVisibility(View.VISIBLE);
            }
        } else {
            recyclerViewTransactions.setVisibility(View.VISIBLE);
            if (tvEmptyState != null) {
                tvEmptyState.setVisibility(View.GONE);
            }
        }
    }

    private List<Transaction> loadTransactionsFromDB() {
        List<Transaction> transactions = new ArrayList<>();
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query("transactions", null, null, null, null, null, "id DESC")) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                transactions.add(new Transaction(id, type, category, amount, date));
            }
        } finally {
            db.close();
        }
        Log.d(TAG, "Total transactions: " + transactions.size());
        return transactions;
    }

    @Override
    public void onTransactionDeleted() {
        allTransactions = loadTransactionsFromDB();
        adapter.updateTransactions(allTransactions);
    }
}