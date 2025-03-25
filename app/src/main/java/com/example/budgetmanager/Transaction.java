package com.example.budgetmanager;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Represents a financial transaction with all fields from the database.
 */
public class Transaction {
    private final long id;
    private final String name; // Maps to 'note' in DB
    private final String type;
    private final String category;
    private final double amount;
    private final String date;
    private final String frequency;
    private final String frequencyValue;
    private final String frequencyUnit;

    public Transaction(long id, String type, String category, double amount, String date,
                       String name, String frequency, String frequencyValue, String frequencyUnit) {
        this.id = id;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.name = name != null ? name : type + " - " + category;
        this.frequency = frequency != null ? frequency : "One-time";
        this.frequencyValue = frequencyValue;
        this.frequencyUnit = frequencyUnit;
    }

    // Simplified constructor for partial data (e.g., recent transactions)
    public Transaction(long id, String type, String category, double amount, String date) {
        this(id, type, category, amount, date, null, null, null, null);
    }

    public long getId() { return id; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public String getName() { return name; }
    public String getFrequency() { return frequency; }
    public String getFrequencyValue() { return frequencyValue; }
    public String getFrequencyUnit() { return frequencyUnit; }

    public long getDateMillis() {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date).getTime();
        } catch (ParseException e) {
            return 0;
        }
    }
}