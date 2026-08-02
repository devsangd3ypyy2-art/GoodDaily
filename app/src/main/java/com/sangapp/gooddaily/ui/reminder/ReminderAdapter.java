package com.sangapp.gooddaily.ui.reminder;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.local.entity.ReminderEntity;
import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.databinding.ItemReminderBinding;
import com.sangapp.gooddaily.util.DateUtils;
import com.sangapp.gooddaily.util.ThemeUtils;

public class ReminderAdapter extends ListAdapter<ReminderEntity, ReminderAdapter.Holder> {
    public interface Listener {
        void onEdit(ReminderEntity entity);
        void onToggle(ReminderEntity entity, boolean enabled);
        void onDelete(ReminderEntity entity);
    }

    private final Listener listener;

    public ReminderAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<ReminderEntity> DIFF = new DiffUtil.ItemCallback<ReminderEntity>() {
        @Override public boolean areItemsTheSame(@NonNull ReminderEntity oldItem, @NonNull ReminderEntity newItem) {
            return oldItem.id == newItem.id;
        }
        @Override public boolean areContentsTheSame(@NonNull ReminderEntity oldItem, @NonNull ReminderEntity newItem) {
            return oldItem.enabled == newItem.enabled
                    && oldItem.hour == newItem.hour
                    && oldItem.minute == newItem.minute
                    && safe(oldItem.title).equals(safe(newItem.title))
                    && safe(oldItem.description).equals(safe(newItem.description))
                    && safe(oldItem.dateKey).equals(safe(newItem.dateKey))
                    && safe(oldItem.repeatType).equals(safe(newItem.repeatType))
                    && safe(oldItem.category).equals(safe(newItem.category));
        }
    };

    private static String safe(String value) { return value == null ? "" : value; }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemReminderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(getItem(position));
    }

    final class Holder extends RecyclerView.ViewHolder {
        private final ItemReminderBinding binding;

        Holder(ItemReminderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ReminderEntity item) {
            String theme = new LocalUserStore(binding.getRoot().getContext()).getThemeKey();
            int accent = ThemeUtils.getPrimaryColor(binding.getRoot().getContext(), theme);
            binding.iconBox.setCardBackgroundColor(accent);
            binding.imgCategory.setImageResource(categoryIcon(item.category));
            binding.imgCategory.setColorFilter(ThemeUtils.getContrastingTextColor(accent));
            ThemeUtils.tintSwitch(binding.switchEnabled, binding.getRoot().getContext(), theme);

            binding.tvTitle.setText(item.title);
            binding.tvDescription.setText(item.description == null || item.description.trim().isEmpty()
                    ? categoryLabel(item.category)
                    : item.description);
            binding.tvSchedule.setText(scheduleLabel(item));
            binding.switchEnabled.setOnCheckedChangeListener(null);
            binding.switchEnabled.setChecked(item.enabled);
            binding.switchEnabled.setOnCheckedChangeListener((button, checked) -> listener.onToggle(item, checked));
            binding.getRoot().setOnClickListener(v -> listener.onEdit(item));
            binding.getRoot().setOnLongClickListener(v -> {
                listener.onDelete(item);
                return true;
            });
        }
    }

    private String scheduleLabel(ReminderEntity item) {
        String time = DateUtils.formatTime(item.hour, item.minute);
        if ("DAILY".equals(item.repeatType)) return "Hằng ngày · " + time;
        if ("WEEKLY".equals(item.repeatType)) return "Hằng tuần · " + DateUtils.formatWeekdayDateKey(item.dateKey) + " · " + time;
        return DateUtils.formatCompactDateKey(item.dateKey) + " · " + time;
    }

    private int categoryIcon(String category) {
        if ("STUDY".equals(category)) return R.drawable.ic_book;
        if ("HEALTH".equals(category)) return R.drawable.ic_health;
        if ("FINANCE".equals(category)) return R.drawable.ic_finance_alert;
        if ("TASK".equals(category)) return R.drawable.ic_check_circle;
        return R.drawable.ic_bell;
    }

    private String categoryLabel(String category) {
        if ("STUDY".equals(category)) return "Học tập";
        if ("HEALTH".equals(category)) return "Sức khỏe";
        if ("FINANCE".equals(category)) return "Tài chính";
        if ("TASK".equals(category)) return "Công việc";
        return "Nhắc nhở chung";
    }
}
