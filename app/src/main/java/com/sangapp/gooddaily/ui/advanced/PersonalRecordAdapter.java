package com.sangapp.gooddaily.ui.advanced;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.sangapp.gooddaily.data.local.entity.PersonalRecordEntity;
import com.sangapp.gooddaily.databinding.ItemPersonalRecordBinding;
import com.sangapp.gooddaily.feature.FeatureDefinition;
import com.sangapp.gooddaily.feature.FeatureCatalog;

import java.util.Locale;

public class PersonalRecordAdapter extends ListAdapter<PersonalRecordEntity, PersonalRecordAdapter.Holder> {
    public interface Listener {
        void onClick(PersonalRecordEntity item);
        void onLongClick(PersonalRecordEntity item);
        void onFavorite(PersonalRecordEntity item);
    }

    private final Listener listener;
    private final FeatureDefinition definition;

    public PersonalRecordAdapter(FeatureDefinition definition, Listener listener) {
        super(DIFF);
        this.definition = definition;
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<PersonalRecordEntity> DIFF = new DiffUtil.ItemCallback<>() {
        @Override public boolean areItemsTheSame(@NonNull PersonalRecordEntity oldItem, @NonNull PersonalRecordEntity newItem) {
            return oldItem.id == newItem.id;
        }
        @Override public boolean areContentsTheSame(@NonNull PersonalRecordEntity oldItem, @NonNull PersonalRecordEntity newItem) {
            return oldItem.updatedAt == newItem.updatedAt && oldItem.favorite == newItem.favorite &&
                    safe(oldItem.title).equals(safe(newItem.title)) && safe(oldItem.status).equals(safe(newItem.status));
        }
        private String safe(String value) { return value == null ? "" : value; }
    };

    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemPersonalRecordBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(getItem(position));
    }

    class Holder extends RecyclerView.ViewHolder {
        private final ItemPersonalRecordBinding binding;
        Holder(ItemPersonalRecordBinding binding) { super(binding.getRoot()); this.binding = binding; }
        void bind(PersonalRecordEntity item) {
            binding.tvRecordTitle.setText(empty(item.title) ? "Không có tiêu đề" : item.title);
            binding.tvRecordDetails.setText(empty(item.details) ? "Không có ghi chú" : item.details);
            binding.imgFavorite.setVisibility(item.favorite ? View.VISIBLE : View.INVISIBLE);

            StringBuilder meta = new StringBuilder();
            if (item.numericValue != 0) meta.append(format(item.numericValue)).append(empty(definition.valueSuffix) ? "" : " " + definition.valueSuffix);
            if (item.secondaryValue != 0) {
                if (meta.length() > 0) meta.append(" · ");
                meta.append(format(item.secondaryValue));
            }
            if (item.countValue != 0) {
                if (meta.length() > 0) meta.append(" · ");
                meta.append(item.countValue).append(" lần");
            }
            if (!empty(item.status)) {
                if (meta.length() > 0) meta.append(" · ");
                meta.append(item.status);
            }
            appendCalculatedMeta(meta, item);
            binding.tvRecordMeta.setText(meta.length() == 0 ? "Chạm để xem và chỉnh sửa" : meta.toString());

            String date = empty(item.dateKey) ? "Không đặt ngày" : item.dateKey;
            if (definition.showTime && (item.startMinutes > 0 || item.endMinutes > 0)) {
                date += " · " + time(item.startMinutes) + "–" + time(item.endMinutes);
            }
            if (!empty(item.tags)) date += " · #" + item.tags.replace(",", " #");
            binding.tvRecordDate.setText(date);

            binding.getRoot().setOnClickListener(v -> listener.onClick(item));
            binding.getRoot().setOnLongClickListener(v -> { listener.onLongClick(item); return true; });
            binding.imgFavorite.setOnClickListener(v -> listener.onFavorite(item));
        }
        private void appendCalculatedMeta(StringBuilder meta, PersonalRecordEntity item) {
            String feature = definition.feature;
            if (FeatureCatalog.DRIVER_SHIFT.equals(feature)) {
                double net = item.numericValue - item.secondaryValue;
                int durationMinutes = item.endMinutes >= item.startMinutes
                        ? item.endMinutes - item.startMinutes
                        : 24 * 60 - item.startMinutes + item.endMinutes;
                if (meta.length() > 0) meta.append("\n");
                meta.append("Lãi ròng ").append(format(net)).append(" ₫");
                if (durationMinutes > 0) meta.append(" · ").append(format(net / (durationMinutes / 60d))).append(" ₫/giờ");
                if (item.countValue > 0) meta.append(" · ").append(format(net / item.countValue)).append(" ₫/cuốc");
            } else if (FeatureCatalog.DRIVER_FUEL.equals(feature)) {
                if (meta.length() > 0) meta.append("\n");
                if (item.secondaryValue > 0) meta.append("Giá ").append(format(item.numericValue / item.secondaryValue)).append(" ₫/lít");
                if (item.secondaryValue > 0 && item.countValue > 0) {
                    if (meta.charAt(meta.length() - 1) != '\n') meta.append(" · ");
                    meta.append(format(item.secondaryValue * 100d / item.countValue)).append(" l/100km");
                }
            } else if (FeatureCatalog.FINANCE_SAVING.equals(feature)
                    || FeatureCatalog.PERSONAL_GOAL.equals(feature)
                    || FeatureCatalog.LEARNING_GOAL.equals(feature)) {
                if (item.numericValue > 0) {
                    double percent = Math.max(0, Math.min(100, item.secondaryValue / item.numericValue * 100));
                    if (meta.length() > 0) meta.append(" · ");
                    meta.append(format(percent)).append("%");
                }
            } else if (FeatureCatalog.FINANCE_BUDGET.equals(feature) && item.numericValue > 0) {
                double percent = Math.max(0, item.secondaryValue / item.numericValue * 100);
                if (meta.length() > 0) meta.append(" · ");
                meta.append("đã dùng ").append(format(percent)).append("%");
            } else if (FeatureCatalog.FINANCE_DEBT.equals(feature) && item.numericValue > 0) {
                double paid = Math.max(0, item.numericValue - item.secondaryValue);
                if (meta.length() > 0) meta.append(" · ");
                meta.append("đã trả ").append(format(paid)).append(" ₫");
            }
        }

        private String format(double value) {
            if (Math.abs(value - Math.rint(value)) < 0.001) return String.format(Locale.US, "%.0f", value);
            return String.format(Locale.US, "%.1f", value);
        }
        private String time(int minutes) { return String.format(Locale.US, "%02d:%02d", minutes / 60, minutes % 60); }
        private boolean empty(String value) { return value == null || value.trim().isEmpty(); }
    }
}
