package com.sangapp.gooddaily.ui.featurehub;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;
import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.databinding.FragmentFeatureHubBinding;
import com.sangapp.gooddaily.feature.FeatureCatalog;
import com.sangapp.gooddaily.feature.FeatureDefinition;
import com.sangapp.gooddaily.util.ThemeUtils;

import java.util.Arrays;
import java.util.List;

public class FeatureHubFragment extends Fragment {
    private FragmentFeatureHubBinding binding;
    private int accent;
    private int containerColor;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFeatureHubBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        LocalUserStore store = new LocalUserStore(requireContext());
        accent = ThemeUtils.getPrimaryColor(requireContext(), store.getThemeKey());
        containerColor = ThemeUtils.getContainerColor(requireContext(), store.getThemeKey());
        ThemeUtils.tintTonalButton(binding.btnGlobalSearch, requireContext(), store.getThemeKey());
        binding.btnGlobalSearch.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.globalSearchFragment));

        addSpecialSection("Tài chính local", Arrays.asList(
                new Special("Ví và tài khoản tiền", "Tự đặt tên, nhập số dư, ẩn tài khoản không dùng.", R.drawable.ic_finance, R.id.localAccountManagerFragment),
                new Special("Tìm và lọc giao dịch", "Lọc theo khoảng ngày, ví tiền, danh mục và từ khóa.", R.drawable.ic_search, R.id.financeSearchFragment),
                new Special("Phân tích tài chính", "Biểu đồ, so sánh kỳ, dự báo và xuất CSV/PDF.", R.drawable.ic_finance_alert, R.id.financeInsightsFragment)
        ));
        addCatalogSection("Tài chính nâng cao", "FINANCE");

        addSpecialSection("Sức khỏe", Arrays.asList(
                new Special("BMR, TDEE và mục tiêu kcal", "Tính nhu cầu năng lượng theo cơ thể và mức vận động.", R.drawable.ic_health, R.id.healthCalculatorFragment),
                new Special("Báo cáo sức khỏe", "Biểu đồ cân nặng và tổng hợp dinh dưỡng tuần, tháng, năm.", R.drawable.ic_chart, R.id.healthReportFragment)
        ));
        addCatalogSection("Theo dõi sức khỏe", "HEALTH");

        addSpecialSection("Lịch và tổ chức", Arrays.asList(
                new Special("Lịch tháng tổng hợp", "Xem ngày có sự kiện, học tập, tài chính và nhật ký.", R.drawable.ic_calendar, R.id.monthCalendarFragment)
        ));
        addCatalogSection("Kế hoạch và công việc", "PLANNER");
        addCatalogSection("Học tập", "LEARNING");
        addSpecialSection("Tiến độ thói quen", Arrays.asList(
                new Special("Heatmap và chuỗi thói quen", "Xem lịch 12 tuần, chuỗi hiện tại, chuỗi dài nhất và tỷ lệ hoàn thành.", R.drawable.ic_check, R.id.habitInsightsFragment)
        ));
        addCatalogSection("Thói quen và mục tiêu", "HABIT");
        addCatalogSection("Mục tiêu cá nhân", "GOAL");
        addCatalogSection("Nhật ký", "JOURNAL");
        addSpecialSection("Dịch học và nghiệm lý", Arrays.asList(
                new Special("Mai Hoa và Lục Hào", "Lập quẻ, luận giải biểu tượng và lưu lịch sử nghiệm lý.", R.drawable.ic_book, R.id.metaphysicsHomeFragment)
        ));
        addSpecialSection("Ca chạy và phương tiện", Arrays.asList(
                new Special("Bảng điều khiển ca chạy", "Doanh thu, lợi nhuận, số đơn, pin và bảo dưỡng xe.", R.drawable.ic_chart, R.id.driverDashboardFragment)
        ));
        addCatalogSection("Nhắc nhở nâng cao", "REMINDER");

        addSpecialSection("Bảo mật và dữ liệu", Arrays.asList(
                new Special("PIN, vân tay và tự khóa", "Bảo vệ app và ẩn nội dung nhạy cảm.", R.drawable.ic_settings, R.id.securityCenterFragment),
                new Special("Backup ZIP có mật khẩu", "Sao lưu database và file đính kèm để chuyển máy.", R.drawable.ic_export, R.id.backupCenterFragment)
        ));
    }

    private void addCatalogSection(String title, String module) {
        List<FeatureDefinition> definitions = FeatureCatalog.byModule(module);
        if (definitions.isEmpty()) return;
        addSectionTitle(title);
        for (FeatureDefinition definition : definitions) {
            addCard(definition.title, definition.description, iconFor(module), v -> {
                Bundle args = new Bundle();
                args.putString(FeatureManagerFragment.ARG_FEATURE, definition.feature);
                Navigation.findNavController(v).navigate(R.id.featureManagerFragment, args);
            });
        }
    }

    private void addSpecialSection(String title, List<Special> specials) {
        addSectionTitle(title);
        for (Special special : specials) addCard(special.title, special.description, special.icon, v -> Navigation.findNavController(v).navigate(special.destination));
    }

    private void addSectionTitle(String text) {
        TextView title = new TextView(requireContext());
        title.setText(text);
        title.setTextSize(18);
        title.setTextColor(getResources().getColor(R.color.on_surface, requireContext().getTheme()));
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(20);
        params.bottomMargin = dp(8);
        binding.featureHubContainer.addView(title, params);
    }

    private void addCard(String titleText, String descriptionText, int iconRes, View.OnClickListener listener) {
        MaterialCardView card = new MaterialCardView(requireContext());
        card.setCardBackgroundColor(getResources().getColor(R.color.surface, requireContext().getTheme()));
        card.setRadius(dp(20));
        card.setCardElevation(0);
        card.setStrokeColor(getResources().getColor(R.color.outline, requireContext().getTheme()));
        card.setStrokeWidth(dp(1));
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(listener);

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(15), dp(15), dp(15), dp(15));

        MaterialCardView iconBox = new MaterialCardView(requireContext());
        iconBox.setRadius(dp(15));
        iconBox.setCardBackgroundColor(containerColor);
        iconBox.setCardElevation(0);
        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(iconRes);
        icon.setColorFilter(accent);
        icon.setPadding(dp(12), dp(12), dp(12), dp(12));
        iconBox.addView(icon, new ViewGroup.LayoutParams(dp(48), dp(48)));
        row.addView(iconBox, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout textWrap = new LinearLayout(requireContext());
        textWrap.setOrientation(LinearLayout.VERTICAL);
        TextView title = new TextView(requireContext());
        title.setText(titleText);
        title.setTextSize(16);
        title.setTextColor(getResources().getColor(R.color.on_surface, requireContext().getTheme()));
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        TextView description = new TextView(requireContext());
        description.setText(descriptionText);
        description.setTextSize(13);
        description.setTextColor(getResources().getColor(R.color.on_surface_variant, requireContext().getTheme()));
        description.setPadding(0, dp(4), 0, 0);
        textWrap.addView(title);
        textWrap.addView(description);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        textParams.leftMargin = dp(12);
        row.addView(textWrap, textParams);

        ImageView arrow = new ImageView(requireContext());
        arrow.setImageResource(R.drawable.ic_arrow_right);
        arrow.setColorFilter(getResources().getColor(R.color.on_surface_variant, requireContext().getTheme()));
        row.addView(arrow, new LinearLayout.LayoutParams(dp(24), dp(24)));
        card.addView(row);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(10);
        binding.featureHubContainer.addView(card, params);
    }

    private int iconFor(String module) {
        switch (module) {
            case "FINANCE": return R.drawable.ic_finance;
            case "HEALTH": return R.drawable.ic_health;
            case "LEARNING": return R.drawable.ic_book;
            case "JOURNAL": return R.drawable.ic_edit;
            case "DRIVER": return R.drawable.ic_arrow_right;
            case "REMINDER": return R.drawable.ic_bell;
            default: return R.drawable.ic_planner;
        }
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private static class Special {
        final String title; final String description; final int icon; final int destination;
        Special(String title, String description, int icon, int destination) {
            this.title = title; this.description = description; this.icon = icon; this.destination = destination;
        }
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
