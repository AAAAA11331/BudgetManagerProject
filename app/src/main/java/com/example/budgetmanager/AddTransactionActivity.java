package com.example.budgetmanager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Calendar;
import com.example.budgetmanager.R;

public class AddTransactionActivity extends BaseActivity {
    private static final String TAG = "AddTransactionActivity";
    private Spinner spinnerType, spinnerCategory, spinnerFrequency, spinnerFrequencyUnit;
    private TextInputEditText editAmount, editDate, editNote, editFrequencyValue;
    private MaterialButton btnSubmit;
    private TextInputLayout frequencyValueLayout;
    private DatabaseHelper dbHelper;
    private ArrayAdapter<String> typeAdapter, categoryAdapter, frequencyAdapter, frequencyUnitAdapter;
    private long transactionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);
        Log.d(TAG, "onCreate called");

        dbHelper = new DatabaseHelper(this);
        spinnerType = findViewById(R.id.spinnerType);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerFrequency = findViewById(R.id.spinnerFrequency);
        spinnerFrequencyUnit = findViewById(R.id.spinnerFrequencyUnit);
        editAmount = findViewById(R.id.editAmount);
        editDate = findViewById(R.id.editDate);
        editNote = findViewById(R.id.editName);
        editFrequencyValue = findViewById(R.id.editFrequencyValue);
        frequencyValueLayout = findViewById(R.id.frequencyValueLayout);
        btnSubmit = findViewById(R.id.btnSubmit);

        Log.d(TAG, "Views initialized: spinnerType=" + (spinnerType != null) +
                ", editAmount=" + (editAmount != null) +
                ", btnSubmit=" + (btnSubmit != null));

        typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"Select a Type", "Expense", "Income"});
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"Select a Category"});
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        spinnerCategory.setEnabled(false);

        frequencyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"One-time", "Recurring"});
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(frequencyAdapter);

        editDate.setText(String.format("%04d-%02d-%02d",
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH) + 1,
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));

        frequencyUnitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"Days", "Weeks", "Months", "Years"});
        frequencyUnitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequencyUnit.setAdapter(frequencyUnitAdapter);

        transactionId = getIntent().getLongExtra("transaction_id", -1);
        if (transactionId != -1) {
            loadTransaction(transactionId);
            btnSubmit.setText("Update");
        } else {
            btnSubmit.setText("Submit");
        }

        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedType = spinnerType.getSelectedItem().toString();
                if (selectedType.equals("Expense")) {
                    categoryAdapter = new ArrayAdapter<>(AddTransactionActivity.this,
                            android.R.layout.simple_spinner_item,
                            new String[]{"Select a Category", "Food", "Transport", "Shopping", "Bills", "Others"});
                    spinnerCategory.setEnabled(true);
                } else if (selectedType.equals("Income")) {
                    categoryAdapter = new ArrayAdapter<>(AddTransactionActivity.this,
                            android.R.layout.simple_spinner_item,
                            new String[]{"Select a Category", "Salary", "Investment", "Gift", "Others"});
                    spinnerCategory.setEnabled(true);
                } else {
                    categoryAdapter = new ArrayAdapter<>(AddTransactionActivity.this,
                            android.R.layout.simple_spinner_item, new String[]{"Select a Category"});
                    spinnerCategory.setEnabled(false);
                }
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(categoryAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String freqChoice = spinnerFrequency.getSelectedItem().toString();
                if (freqChoice.equals("Recurring")) {
                    frequencyValueLayout.setVisibility(View.VISIBLE);
                } else {
                    frequencyValueLayout.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        editDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(AddTransactionActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        String formattedDate = String.format("%04d-%02d-%02d", year, (month + 1), dayOfMonth);
                        editDate.setText(formattedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        btnSubmit.setOnClickListener(v -> confirmSubmission());

        setupBottomNavigation();
    }

    @Override
    protected int getSelectedItemId() {
        return R.id.nav_dashboard; // Not a main nav item, so default to dashboard
    }

    @Override
    protected Class<?> getActivityClass() {
        return AddTransactionActivity.class;
    }

    private void loadTransaction(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("transactions", null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToFirst()) {
            String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
            String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
            double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            String note = cursor.getString(cursor.getColumnIndexOrThrow("note"));
            String frequency = cursor.getString(cursor.getColumnIndexOrThrow("frequency"));
            String frequencyValue = cursor.getString(cursor.getColumnIndexOrThrow("frequency_value"));
            String frequencyUnit = cursor.getString(cursor.getColumnIndexOrThrow("frequency_unit"));

            int typePos = typeAdapter.getPosition(type);
            if (typePos != -1) {
                spinnerType.setSelection(typePos);
            }

            if (type.equals("Expense")) {
                categoryAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item,
                        new String[]{"Select a Category", "Food", "Transport", "Shopping", "Bills", "Others"});
            } else if (type.equals("Income")) {
                categoryAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item,
                        new String[]{"Select a Category", "Salary", "Investment", "Gift", "Others"});
            }
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(categoryAdapter);
            int catPos = categoryAdapter.getPosition(category);
            if (catPos != -1) {
                spinnerCategory.setSelection(catPos);
            }

            editAmount.setText(String.valueOf(amount));
            editDate.setText(date);
            editNote.setText(note != null ? note : "");

            int freqPos = frequencyAdapter.getPosition(frequency);
            if (freqPos != -1) {
                spinnerFrequency.setSelection(freqPos);
            }

            if (frequency != null && frequency.equals("Recurring")) {
                frequencyValueLayout.setVisibility(View.VISIBLE);
                editFrequencyValue.setText(frequencyValue != null ? frequencyValue : "");

                int unitPos = frequencyUnitAdapter.getPosition(frequencyUnit);
                if (unitPos != -1) {
                    spinnerFrequencyUnit.setSelection(unitPos);
                }
            }
        }
        cursor.close();
        db.close();
    }

    private void confirmSubmission() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Transaction")
                .setMessage("Are you sure you want to " + (transactionId == -1 ? "add" : "update") + " this transaction?")
                .setPositiveButton("Yes", (dialog, which) -> submitTransaction())
                .setNegativeButton("No", null)
                .show();
    }

    private void submitTransaction() {
        try {
            String type = spinnerType.getSelectedItem().toString();
            String category = spinnerCategory.getSelectedItem().toString();
            String amountStr = editAmount.getText().toString();
            String date = editDate.getText().toString();
            String note = editNote.getText().toString();
            String frequency = spinnerFrequency.getSelectedItem().toString();
            String frequencyValue = "";
            String frequencyUnit = "";

            if (frequency.equals("Recurring")) {
                frequencyValue = editFrequencyValue.getText().toString();
                frequencyUnit = spinnerFrequencyUnit.getSelectedItem().toString();
            }

            if (type.equals("Select a Type")) {
                Toast.makeText(this, "Please select a transaction type", Toast.LENGTH_SHORT).show();
                return;
            }
            if (category.equals("Select a Category")) {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
                return;
            }
            if (amountStr.isEmpty()) {
                editAmount.setError("Amount is required");
                return;
            }
            if (date.isEmpty()) {
                editDate.setError("Date is required");
                return;
            }
            if (frequency.equals("Recurring") && frequencyValue.isEmpty()) {
                editFrequencyValue.setError("Frequency value is required");
                return;
            }

            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                editAmount.setError("Amount must be greater than 0");
                return;
            }

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("type", type);
            values.put("category", category);
            values.put("amount", amount);
            values.put("date", date);
            values.put("note", note);
            values.put("frequency", frequency);
            values.put("frequency_value", frequencyValue);
            values.put("frequency_unit", frequencyUnit);

            long result;
            if (transactionId == -1) {
                result = db.insert("transactions", null, values);
            } else {
                result = db.update("transactions", values, "id = ?", new String[]{String.valueOf(transactionId)});
            }
            db.close();

            if (result != -1) {
                Toast.makeText(this, transactionId == -1 ? "Transaction added" : "Transaction updated", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to save transaction", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            editAmount.setError("Invalid amount or frequency format");
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}