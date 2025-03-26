package com.example.budgetmanager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.example.budgetmanager.R;

public class MonthlyReportActivity extends BaseActivity {
    private static final String TAG = "MonthlyReport";
    private Spinner spinnerMonth;
    private TableLayout tableLayout;
    private TextView tvNet, tvEmptyState;
    private DatabaseHelper dbHelper;
    private String selectedMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_report);

        dbHelper = new DatabaseHelper(this);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        tableLayout = findViewById(R.id.tableLayoutReport);
        tvNet = findViewById(R.id.tvNet);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        MaterialButton btnExport = findViewById(R.id.btnExport);

        List<String> months = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 12; i++) {
            months.add(String.format("%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1));
            cal.add(Calendar.MONTH, -1);
        }
        spinnerMonth.setAdapter(new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, months));

        spinnerMonth.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                selectedMonth = months.get(position);
                generateMonthlyReport(selectedMonth);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnExport.setOnClickListener(v -> {
            String filePath = getExternalFilesDir(null) + "/MonthlyReport_" + selectedMonth + ".csv";
            dbHelper.exportDatabaseToCSV(this, filePath);
        });

        setupBottomNavigation();
    }

    @Override
    protected int getSelectedItemId() {
        return R.id.nav_reports;
    }


    protected Class<?> getActivityClass() {
        return MonthlyReportActivity.class;
    }

    private void generateMonthlyReport(String yearMonth) {
        tableLayout.removeAllViews();
        TableRow headerRow = new TableRow(this);
        headerRow.addView(createTextView("Category"));
        headerRow.addView(createTextView("Income"));
        headerRow.addView(createTextView("Expense"));
        tableLayout.addView(headerRow);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date reportStart, reportEnd;
        try {
            reportStart = sdf.parse(yearMonth + "-01");
            Calendar cal = Calendar.getInstance();
            cal.setTime(reportStart);
            cal.add(Calendar.MONTH, 1);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            reportEnd = cal.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        double totalIncome = 0.0, totalExpense = 0.0;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query("transactions", null, null, null, null, null, null)) {
            int rowCount = 0;
            while (cursor.moveToNext()) {
                String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                String frequency = cursor.getString(cursor.getColumnIndexOrThrow("frequency"));

                dateStr = normalizeDate(dateStr);
                int occurrences = "Recurring".equals(frequency) ?
                        countOccurrencesInMonth(dateStr, cursor.getString(cursor.getColumnIndexOrThrow("frequency_value")), reportStart, reportEnd) :
                        (isDateInRange(dateStr, reportStart, reportEnd) ? 1 : 0);

                if (occurrences > 0) {
                    double effectiveAmount = amount * occurrences;
                    TableRow row = new TableRow(this);
                    row.addView(createTextView(category));
                    TextView tvInc = createTextView("");
                    TextView tvExp = createTextView("");
                    if ("Income".equalsIgnoreCase(type)) {
                        tvInc.setText(String.format("$%.2f", effectiveAmount));
                        tvInc.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        totalIncome += effectiveAmount;
                    } else {
                        tvExp.setText(String.format("$%.2f", effectiveAmount));
                        tvExp.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        totalExpense += effectiveAmount;
                    }
                    row.addView(tvInc);
                    row.addView(tvExp);
                    tableLayout.addView(row);
                    rowCount++;
                }
            }
            if (rowCount == 0) {
                tableLayout.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
            } else {
                tableLayout.setVisibility(View.VISIBLE);
                tvEmptyState.setVisibility(View.GONE);
            }
        } finally {
            db.close();
        }

        double net = totalIncome - totalExpense;
        tvNet.setText(String.format("Net for %s: $%.2f", yearMonth, net));
    }

    private TextView createTextView(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(16, 16, 16, 16);
        return tv;
    }

    private String normalizeDate(String dateStr) {
        String[] parts = dateStr.split("-");
        if (parts.length != 3) return dateStr;
        return String.format("%04d-%02d-%02d", Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    private boolean isDateInRange(String dateStr, Date start, Date end) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr);
            return !date.before(start) && !date.after(end);
        } catch (ParseException e) {
            return false;
        }
    }

    private int countOccurrencesInMonth(String startDateStr, String freqValueStr, Date reportStart, Date reportEnd) {
        try {
            Date startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(startDateStr);
            int intervalDays = Integer.parseInt(freqValueStr);
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            int count = 0;
            while (cal.getTime().before(reportStart)) {
                cal.add(Calendar.DAY_OF_MONTH, intervalDays);
            }
            while (!cal.getTime().after(reportEnd)) {
                count++;
                cal.add(Calendar.DAY_OF_MONTH, intervalDays);
            }
            return count;
        } catch (Exception e) {
            return 0;
        }
    }
}