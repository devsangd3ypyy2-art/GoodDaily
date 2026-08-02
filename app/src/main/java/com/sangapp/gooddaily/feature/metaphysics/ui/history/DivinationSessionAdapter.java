package com.sangapp.gooddaily.feature.metaphysics.ui.history;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.sangapp.gooddaily.databinding.ItemDivinationSessionBinding;
import com.sangapp.gooddaily.feature.metaphysics.data.DivinationSessionEntity;
import com.sangapp.gooddaily.util.DateUtils;

public class DivinationSessionAdapter extends ListAdapter<DivinationSessionEntity, DivinationSessionAdapter.Holder> {
    public interface Listener {
        void onClick(DivinationSessionEntity item);
        void onLongClick(DivinationSessionEntity item);
    }

    private final Listener listener;

    public DivinationSessionAdapter(Listener listener) {
        super(new DiffUtil.ItemCallback<DivinationSessionEntity>() {
            @Override public boolean areItemsTheSame(@NonNull DivinationSessionEntity oldItem, @NonNull DivinationSessionEntity newItem) { return oldItem.id == newItem.id; }
            @Override public boolean areContentsTheSame(@NonNull DivinationSessionEntity oldItem, @NonNull DivinationSessionEntity newItem) { return oldItem.updatedAt == newItem.updatedAt; }
        });
        this.listener = listener;
    }

    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemDivinationSessionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(getItem(position));
    }

    class Holder extends RecyclerView.ViewHolder {
        private final ItemDivinationSessionBinding binding;
        Holder(ItemDivinationSessionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(DivinationSessionEntity item) {
            String systemLabel = "MAI_HOA".equals(item.system) ? "MAI HOA"
                    : ("LIU_HAO".equals(item.system) ? "LỤC HÀO" : "GHI CHÉP CŨ");
            binding.tvDivinationSystem.setText(systemLabel);
            binding.tvDivinationDate.setText(DateUtils.formatDateTime(item.castTime) + " · " + safe(item.lunarText));
            binding.tvDivinationQuestion.setText(safe(item.question));
            binding.tvDivinationHexagrams.setText("Chủ: " + safe(item.baseHexagramName) + "  →  Biến: " + safe(item.changedHexagramName));
            binding.tvDivinationStatus.setText("DA_KIEM_CHUNG".equals(item.status) ? "✓ Đã nghiệm lý" : "Đang chờ kiểm chứng");
            binding.getRoot().setOnClickListener(v -> listener.onClick(item));
            binding.getRoot().setOnLongClickListener(v -> { listener.onLongClick(item); return true; });
        }
    }

    private static String safe(String text) { return text == null ? "" : text; }
}
