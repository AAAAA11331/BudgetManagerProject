package com.example.budgetmanager;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private List<Transaction> transactions;
    private OnTransactionChangeListener listener;

    public interface OnTransactionChangeListener {
        void onTransactionDeleted();
    }

    public TransactionAdapter(List<Transaction> transactions, OnTransactionChangeListener listener) {
        this.transactions = transactions != null ? transactions : new ArrayList<>();
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        holder.tvName.setText(transaction.getName());
        holder.tvType.setText(transaction.getType());
        holder.tvCategory.setText(transaction.getCategory());
        holder.tvAmount.setText(String.format("$%.2f", transaction.getAmount()));

        // Set type icon and amount color based on transaction type
        if ("Income".equalsIgnoreCase(transaction.getType())) {
            holder.ivTypeIcon.setImageResource(R.drawable.ic_positive_balance);
            holder.tvAmount.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.ivTypeIcon.setImageResource(R.drawable.ic_negative_balance);
            holder.tvAmount.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
        }

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Delete Transaction")
                    .setMessage("Are you sure you want to delete this transaction?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        DatabaseHelper dbHelper = new DatabaseHelper(holder.itemView.getContext());
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        int rowsDeleted = db.delete("transactions", "id = ?",
                                new String[]{String.valueOf(transaction.getId())});
                        db.close();
                        if (rowsDeleted > 0) {
                            transactions.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, transactions.size());
                            if (listener != null) {
                                listener.onTransactionDeleted();
                            }
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        holder.btnEdit.setOnClickListener(v -> startEditActivity(holder.itemView, transaction.getId()));
        holder.itemView.setOnLongClickListener(v -> {
            startEditActivity(holder.itemView, transaction.getId());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void updateTransactions(List<Transaction> newTransactions) {
        this.transactions = newTransactions != null ? newTransactions : new ArrayList<>();
        notifyDataSetChanged();
    }

    private void startEditActivity(View itemView, long transactionId) {
        Intent intent = new Intent(itemView.getContext(), AddTransactionActivity.class);
        intent.putExtra("transaction_id", transactionId);
        itemView.getContext().startActivity(intent);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivTypeIcon; // Declare the ImageView
        TextView tvName, tvType, tvCategory, tvAmount;
        MaterialButton btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            ivTypeIcon = itemView.findViewById(R.id.ivTypeIcon); // Initialize it
            tvName = itemView.findViewById(R.id.tvName);
            tvType = itemView.findViewById(R.id.tvType);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}