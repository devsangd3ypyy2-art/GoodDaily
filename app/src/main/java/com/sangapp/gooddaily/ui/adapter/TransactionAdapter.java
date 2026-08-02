package com.sangapp.gooddaily.ui.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.local.entity.TransactionEntity;
import com.sangapp.gooddaily.databinding.ItemTransactionBinding;
import com.sangapp.gooddaily.util.DateUtils;
import com.sangapp.gooddaily.util.MoneyUtils;

public class TransactionAdapter extends ListAdapter<TransactionEntity, TransactionAdapter.Holder> {
    public interface Listener {
        void onClick(TransactionEntity entity);
        void onLongClick(TransactionEntity entity);
    }

    private final Listener listener;

    public TransactionAdapter(Listener listener) {
        super(new DiffUtil.ItemCallback<TransactionEntity>() {
            @Override public boolean areItemsTheSame(@NonNull TransactionEntity oldItem, @NonNull TransactionEntity newItem) {
                return oldItem.id == newItem.id;
            }

            @Override public boolean areContentsTheSame(@NonNull TransactionEntity oldItem, @NonNull TransactionEntity newItem) {
                return oldItem.amount == newItem.amount
                        && safe(oldItem.type).equals(safe(newItem.type))
                        && safe(oldItem.note).equals(safe(newItem.note))
                        && safe(oldItem.category).equals(safe(newItem.category))
                        && safe(oldItem.account).equals(safe(newItem.account))
                        && oldItem.transactionTime == newItem.transactionTime;
            }
        });
        this.listener = listener;
    }

    private static String safe(String value) { return value == null ? "" : value; }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemTransactionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(getItem(position));
    }

    class Holder extends RecyclerView.ViewHolder {
        private final ItemTransactionBinding binding;

        Holder(ItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(TransactionEntity entity) {
            boolean income = "INCOME".equals(entity.type);
            int color = ContextCompat.getColor(binding.getRoot().getContext(), income ? R.color.income : R.color.expense);
            int container = ContextCompat.getColor(binding.getRoot().getContext(), income ? R.color.income_container : R.color.expense_container);
            binding.symbolBox.setCardBackgroundColor(container);
            binding.imgSymbol.setImageResource(income ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_up);
            binding.imgSymbol.setImageTintList(ColorStateList.valueOf(color));
            binding.tvCategory.setText(entity.category);
            binding.tvNote.setText(entity.note == null || entity.note.isEmpty() ? "Không có ghi chú" : entity.note);
            binding.tvDateAccount.setText(DateUtils.formatDateTime(entity.transactionTime) + " · " + accountName(entity.account));
            binding.tvAmount.setText((income ? "+" : "−") + MoneyUtils.format(entity.amount));
            binding.tvAmount.setTextColor(color);
            binding.getRoot().setOnClickListener(v -> listener.onClick(entity));
            binding.getRoot().setOnLongClickListener(v -> {
                listener.onLongClick(entity);
                return true;
            });
        }

        private String accountName(String account) {
            if ("BANK".equals(account)) return "Ngân hàng";
            if ("EWALLET".equals(account)) return "Ví điện tử";
            return "Tiền mặt";
        }
    }
}
