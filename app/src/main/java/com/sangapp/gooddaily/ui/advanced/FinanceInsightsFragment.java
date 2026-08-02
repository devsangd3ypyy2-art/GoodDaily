package com.sangapp.gooddaily.ui.advanced;

import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.local.GoodDailyDatabase;
import com.sangapp.gooddaily.data.local.entity.CategoryTotal;
import com.sangapp.gooddaily.data.local.entity.TransactionEntity;
import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.databinding.FragmentFinanceInsightsBinding;
import com.sangapp.gooddaily.util.AppExecutors;
import com.sangapp.gooddaily.util.MoneyUtils;
import com.sangapp.gooddaily.util.ThemeUtils;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FinanceInsightsFragment extends Fragment {
    private FragmentFinanceInsightsBinding binding;
    private List<TransactionEntity> transactions = new ArrayList<>();

    private final ActivityResultLauncher<String> createCsv = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("text/csv"), uri -> { if (uri != null) exportCsv(uri); });

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFinanceInsightsBinding.inflate(inflater, container, false); return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        LocalUserStore store = new LocalUserStore(requireContext());
        ThemeUtils.tintFilledButton(binding.btnExportAllFinanceCsv, requireContext(), store.getThemeKey());
        ThemeUtils.tintTonalButton(binding.btnOpenFinancePdf, requireContext(), store.getThemeKey());
        binding.btnExportAllFinanceCsv.setOnClickListener(v -> createCsv.launch("GoodDaily_TaiChinh_" + new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date()) + ".csv"));
        binding.btnOpenFinancePdf.setOnClickListener(v -> {
            Bundle args = new Bundle(); args.putString("focus", "history");
            Navigation.findNavController(v).navigate(R.id.financeFragment, args);
        });
        load();
    }

    private void load() {
        AppExecutors.io().execute(() -> {
            GoodDailyDatabase db = GoodDailyDatabase.get(requireContext());
            Calendar now = Calendar.getInstance();
            long currentStart = monthStart(now, 0); long currentEnd = monthEnd(now, 0);
            long previousStart = monthStart(now, -1); long previousEnd = monthEnd(now, -1);
            double currentIncome = db.transactionDao().totalByTypeRangeSync("INCOME", currentStart, currentEnd);
            double currentExpense = db.transactionDao().totalByTypeRangeSync("EXPENSE", currentStart, currentEnd);
            double previousIncome = db.transactionDao().totalByTypeRangeSync("INCOME", previousStart, previousEnd);
            double previousExpense = db.transactionDao().totalByTypeRangeSync("EXPENSE", previousStart, previousEnd);
            List<CategoryTotal> categories = db.transactionDao().getExpenseByCategorySync(currentStart, currentEnd);
            transactions = db.transactionDao().getAllSync();
            int day = now.get(Calendar.DAY_OF_MONTH);
            int days = now.getActualMaximum(Calendar.DAY_OF_MONTH);
            double avgDailyExpense = day == 0 ? 0 : currentExpense / day;
            double forecastExpense = currentExpense + avgDailyExpense * Math.max(0, days - day);
            requireActivity().runOnUiThread(() -> render(currentIncome, currentExpense, previousIncome, previousExpense, categories, avgDailyExpense, forecastExpense));
        });
    }

    private void render(double ci, double ce, double pi, double pe, List<CategoryTotal> categories, double daily, double forecast) {
        if (binding == null) return;
        binding.tvFinanceComparison.setText("Thu tháng này: " + MoneyUtils.format(ci) + "\nChi tháng này: " + MoneyUtils.format(ce)
                + "\nThu tháng trước: " + MoneyUtils.format(pi) + "\nChi tháng trước: " + MoneyUtils.format(pe)
                + "\nBiến động chi: " + percent(ce, pe));
        binding.financePieChart.setData(categories);
        binding.categoryLegendContainer.removeAllViews();
        if (categories == null || categories.isEmpty()) {
            binding.categoryLegendContainer.addView(legend("Chưa có khoản chi trong tháng."));
        } else {
            for (CategoryTotal item : categories) binding.categoryLegendContainer.addView(legend(item.category + ": " + MoneyUtils.format(item.total)));
        }
        binding.tvFinanceForecast.setText("Chi trung bình mỗi ngày: " + MoneyUtils.format(daily)
                + "\nDự báo tổng chi cuối tháng: " + MoneyUtils.format(forecast)
                + "\nGợi ý: đặt ngân sách danh mục và giảm các nhóm chi lớn nhất để giữ số dư an toàn.");
    }

    private TextView legend(String text) { TextView v = new TextView(requireContext()); v.setText(text); v.setTextColor(getResources().getColor(R.color.on_surface_variant, requireContext().getTheme())); v.setTextSize(13); v.setGravity(Gravity.START); v.setPadding(0, dp(5), 0, dp(5)); return v; }
    private String percent(double current, double previous) { if (previous == 0) return current == 0 ? "0%" : "+100%"; double p=(current-previous)/previous*100; return String.format(Locale.US, "%+.1f%%", p); }

    private long monthStart(Calendar base, int offset) { Calendar c=(Calendar)base.clone(); c.add(Calendar.MONTH,offset); c.set(Calendar.DAY_OF_MONTH,1); c.set(Calendar.HOUR_OF_DAY,0); c.set(Calendar.MINUTE,0); c.set(Calendar.SECOND,0); c.set(Calendar.MILLISECOND,0); return c.getTimeInMillis(); }
    private long monthEnd(Calendar base, int offset) { Calendar c=(Calendar)base.clone(); c.add(Calendar.MONTH,offset+1); c.set(Calendar.DAY_OF_MONTH,1); c.set(Calendar.HOUR_OF_DAY,0); c.set(Calendar.MINUTE,0); c.set(Calendar.SECOND,0); c.set(Calendar.MILLISECOND,0); return c.getTimeInMillis()-1; }

    private void exportCsv(Uri uri) {
        List<TransactionEntity> snapshot = new ArrayList<>(transactions);
        AppExecutors.io().execute(() -> {
            try (OutputStream out = requireContext().getContentResolver().openOutputStream(uri)) {
                if (out == null) throw new IllegalStateException("Không mở được file");
                SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));
                StringBuilder csv = new StringBuilder("id,type,amount,category,account,note,date\n");
                for (TransactionEntity t : snapshot) csv.append(t.id).append(',').append(q(t.type)).append(',').append(t.amount).append(',').append(q(t.category)).append(',').append(q(t.account)).append(',').append(q(t.note)).append(',').append(q(fmt.format(new Date(t.transactionTime)))).append('\n');
                out.write(csv.toString().getBytes(StandardCharsets.UTF_8));
                requireActivity().runOnUiThread(() -> toast("Đã xuất toàn bộ giao dịch CSV."));
            } catch (Exception e) { requireActivity().runOnUiThread(() -> toast("Xuất CSV thất bại: " + e.getMessage())); }
        });
    }
    private String q(String s) { return "\"" + (s == null ? "" : s.replace("\"","\"\"")) + "\""; }
    private int dp(int v) { return Math.round(v*getResources().getDisplayMetrics().density); }
    private void toast(String s) { Toast.makeText(requireContext(), s, Toast.LENGTH_LONG).show(); }
    @Override public void onDestroyView() { super.onDestroyView(); binding=null; }
}
