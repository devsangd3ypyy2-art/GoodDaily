package com.sangapp.gooddaily.feature.driver.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.databinding.DialogBatteryChargeBinding;
import com.sangapp.gooddaily.databinding.DialogDriverShiftBinding;
import com.sangapp.gooddaily.databinding.DialogFuelLogBinding;
import com.sangapp.gooddaily.databinding.DialogMaintenanceRecordBinding;
import com.sangapp.gooddaily.databinding.DialogVehicleBinding;
import com.sangapp.gooddaily.databinding.FragmentDriverDashboardBinding;
import com.sangapp.gooddaily.feature.driver.data.DriverSessionStore;
import com.sangapp.gooddaily.feature.driver.data.entity.BatteryChargeEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.DriverShiftEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.FuelLogEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.MaintenanceRecordEntity;
import com.sangapp.gooddaily.feature.driver.data.entity.VehicleEntity;
import com.sangapp.gooddaily.feature.driver.domain.ShiftProfitCalculator;
import com.sangapp.gooddaily.util.DateUtils;
import com.sangapp.gooddaily.util.MoneyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DriverDashboardFragment extends Fragment {
    private FragmentDriverDashboardBinding binding;
    private DriverViewModel viewModel;
    private DriverSessionStore sessionStore;
    private List<DriverShiftEntity> shifts = new ArrayList<>();
    private List<VehicleEntity> vehicles = new ArrayList<>();
    private List<MaintenanceRecordEntity> maintenance = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDriverDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(DriverViewModel.class);
        sessionStore = new DriverSessionStore(requireContext());
        viewModel.shifts().observe(getViewLifecycleOwner(), value -> {
            shifts = value == null ? new ArrayList<>() : value;
            renderShifts();
            renderSummary();
        });
        viewModel.vehicles().observe(getViewLifecycleOwner(), value -> {
            vehicles = value == null ? new ArrayList<>() : value;
            renderVehicles();
        });
        viewModel.maintenance().observe(getViewLifecycleOwner(), value -> {
            maintenance = value == null ? new ArrayList<>() : value;
            renderMaintenance();
        });

        binding.btnToggleLiveShift.setOnClickListener(v -> toggleLiveShift());
        updateLiveShiftUi();
        binding.btnAddDriverShift.setOnClickListener(v -> showShiftForm(null));
        binding.btnAddVehicle.setOnClickListener(v -> showVehicleForm());
        binding.btnAddCharge.setOnClickListener(v -> showChargeForm());
        binding.btnAddMaintenance.setOnClickListener(v -> showMaintenanceForm());
        binding.btnAddFuel.setOnClickListener(v -> showFuelForm());
    }

    private void toggleLiveShift() {
        if (!sessionStore.isRunning()) {
            sessionStore.start(System.currentTimeMillis());
            updateLiveShiftUi();
            toast("Đã bắt đầu ca. Good Daily sẽ giữ giờ bắt đầu ngay cả khi bạn đóng app.");
            return;
        }
        long start = sessionStore.getStartTime();
        long end = System.currentTimeMillis();
        DriverShiftEntity draft = new DriverShiftEntity();
        draft.dateKey = DateUtils.dateKey(start);
        draft.startTime = start;
        draft.endTime = Math.max(end, start + 60_000L);
        draft.status = "DRAFT";
        draft.createdAt = start;
        draft.updatedAt = end;
        showShiftForm(draft, true);
    }

    private void updateLiveShiftUi() {
        if (binding == null || sessionStore == null) return;
        if (sessionStore.isRunning()) {
            long start = sessionStore.getStartTime();
            binding.tvLiveShiftStatus.setText("Đang chạy từ " + DateUtils.formatDateTime(start)
                    + ". Nhấn kết thúc để nhập doanh thu và chi phí.");
            binding.btnToggleLiveShift.setText("Kết thúc ca");
            binding.btnToggleLiveShift.setIconResource(R.drawable.ic_stop);
        } else {
            binding.tvLiveShiftStatus.setText("Chưa bắt đầu ca. Bộ đếm giờ được lưu cục bộ trên máy.");
            binding.btnToggleLiveShift.setText("Bắt đầu ca");
            binding.btnToggleLiveShift.setIconResource(R.drawable.ic_play);
        }
    }

    private void renderSummary() {
        double net = 0;
        int orders = 0;
        double km = 0;
        for (DriverShiftEntity shift : shifts) {
            net += ShiftProfitCalculator.netProfit(shift);
            orders += shift.orderCount;
            km += shift.distanceKm;
        }
        binding.tvDriverNetProfit.setText(MoneyUtils.format(net));
        binding.tvDriverSummary.setText(String.format(Locale.getDefault(), "%d / %d / %.1f km", shifts.size(), orders, km));
    }

    private void renderShifts() {
        binding.containerDriverShifts.removeAllViews();
        if (shifts.isEmpty()) {
            binding.containerDriverShifts.addView(emptyText("Chưa có ca chạy. Nhấn “Thêm ca chạy” để bắt đầu ghi nhận."));
            return;
        }
        int limit = Math.min(shifts.size(), 12);
        for (int i = 0; i < limit; i++) {
            DriverShiftEntity shift = shifts.get(i);
            String title = DateUtils.formatCompactDateKey(shift.dateKey) + " · " + safe(shift.area, "Ca chạy");
            String details = "Doanh thu " + MoneyUtils.format(ShiftProfitCalculator.grossIncome(shift))
                    + " · Chi phí " + MoneyUtils.format(ShiftProfitCalculator.totalCost(shift))
                    + "\n" + shift.orderCount + " đơn · " + String.format(Locale.getDefault(), "%.1f km", shift.distanceKm)
                    + " · Lãi " + MoneyUtils.format(ShiftProfitCalculator.netProfit(shift));
            MaterialCardView card = recordCard(title, details);
            card.setOnClickListener(v -> showShiftForm(shift));
            card.setOnLongClickListener(v -> {
                confirmDeleteShift(shift);
                return true;
            });
            binding.containerDriverShifts.addView(card, cardParams());
        }
    }

    private void renderVehicles() {
        binding.containerVehicles.removeAllViews();
        if (vehicles.isEmpty()) {
            binding.containerVehicles.addView(emptyText("Chưa có phương tiện. Tạo xe để theo dõi pin, kilomet và bảo dưỡng."));
            return;
        }
        for (VehicleEntity vehicle : vehicles) {
            String title = vehicle.name + (isBlank(vehicle.plateNumber) ? "" : " · " + vehicle.plateNumber);
            String details = safe(vehicle.vehicleType, "Phương tiện") + " · " + safe(vehicle.energyType, "Chưa chọn năng lượng")
                    + "\nOdo " + String.format(Locale.getDefault(), "%.1f km", vehicle.currentOdometerKm)
                    + (vehicle.batteryNominalCapacity > 0 ? " · Pin " + vehicle.batteryNominalCapacity + " kWh" : "");
            binding.containerVehicles.addView(recordCard(title, details), cardParams());
        }
    }

    private void renderMaintenance() {
        binding.containerMaintenance.removeAllViews();
        if (maintenance.isEmpty()) {
            binding.containerMaintenance.addView(emptyText("Chưa có lịch bảo dưỡng. Bạn có thể thêm kiểm tra phanh, lốp, ốc, pin hoặc hệ thống điện."));
            return;
        }
        int limit = Math.min(maintenance.size(), 10);
        for (int i = 0; i < limit; i++) {
            MaintenanceRecordEntity item = maintenance.get(i);
            String due = item.nextDueAt > 0 ? DateUtils.formatShortDate(item.nextDueAt) : "Chưa đặt ngày";
            String details = "Chi phí " + MoneyUtils.format(item.cost) + " · Nhắc " + due
                    + (item.nextDueOdometerKm > 0 ? " / " + item.nextDueOdometerKm + " km" : "")
                    + "\n" + safe(item.note, "Không có ghi chú");
            binding.containerMaintenance.addView(recordCard(safe(item.title, "Bảo dưỡng"), details), cardParams());
        }
    }

    private void showShiftForm(@Nullable DriverShiftEntity existing) {
        showShiftForm(existing, false);
    }

    private void showShiftForm(@Nullable DriverShiftEntity existing, boolean fromLiveSession) {
        DialogDriverShiftBinding form = DialogDriverShiftBinding.inflate(getLayoutInflater());
        form.edtShiftDate.setText(existing == null ? DateUtils.dateKey() : existing.dateKey);
        form.edtShiftStart.setText(existing == null ? "08:00" : time(existing.startTime));
        form.edtShiftEnd.setText(existing == null ? "12:00" : time(existing.endTime));
        if (existing != null) {
            form.edtShiftArea.setText(existing.area);
            form.edtShiftRevenue.setText(number(existing.revenue));
            form.edtShiftBonus.setText(number(existing.bonus + existing.tips));
            form.edtShiftOrders.setText(String.valueOf(existing.orderCount));
            form.edtShiftKm.setText(number(existing.distanceKm));
            form.edtShiftCosts.setText(number(ShiftProfitCalculator.totalCost(existing)));
            form.edtShiftNote.setText(existing.note);
        }
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(existing == null ? "Thêm ca chạy" : (existing.id == 0 ? "Hoàn tất ca chạy" : "Sửa ca chạy"))
                .setView(form.getRoot())
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener(x -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            try {
                DriverShiftEntity entity = existing == null ? new DriverShiftEntity() : existing;
                String date = text(form.edtShiftDate.getText());
                long start = parseDateTime(date, text(form.edtShiftStart.getText()));
                long end = parseDateTime(date, text(form.edtShiftEnd.getText()));
                if (end <= start) end += 86_400_000L;
                entity.dateKey = date;
                entity.startTime = start;
                entity.endTime = end;
                entity.area = text(form.edtShiftArea.getText());
                entity.revenue = decimal(form.edtShiftRevenue.getText());
                entity.bonus = decimal(form.edtShiftBonus.getText());
                entity.tips = 0;
                entity.orderCount = integer(form.edtShiftOrders.getText());
                entity.distanceKm = decimal(form.edtShiftKm.getText());
                entity.otherCost = decimal(form.edtShiftCosts.getText());
                entity.platformFee = 0;
                entity.energyCost = 0;
                entity.mealCost = 0;
                entity.depreciationCost = 0;
                entity.note = text(form.edtShiftNote.getText());
                entity.status = "CLOSED";
                long now = System.currentTimeMillis();
                if (entity.createdAt == 0) entity.createdAt = now;
                entity.updatedAt = now;
                viewModel.saveShift(entity, () -> {
                    if (fromLiveSession) {
                        sessionStore.clear();
                        updateLiveShiftUi();
                    }
                    toast("Đã lưu ca chạy.");
                });
                dialog.dismiss();
            } catch (Exception error) {
                toast("Kiểm tra ngày, giờ và số tiền đã nhập.");
            }
        }));
        dialog.show();
    }

    private void showVehicleForm() {
        DialogVehicleBinding form = DialogVehicleBinding.inflate(getLayoutInflater());
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Thêm phương tiện")
                .setView(form.getRoot())
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener(x -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = text(form.edtVehicleName.getText());
            if (name.isEmpty()) { toast("Hãy nhập tên xe."); return; }
            VehicleEntity entity = new VehicleEntity();
            entity.name = name;
            entity.plateNumber = text(form.edtVehiclePlate.getText());
            entity.vehicleType = text(form.edtVehicleType.getText());
            entity.energyType = text(form.edtVehicleEnergyType.getText());
            entity.currentOdometerKm = decimal(form.edtVehicleOdometer.getText());
            entity.batteryNominalCapacity = decimal(form.edtVehicleBattery.getText());
            entity.active = true;
            entity.purchaseDate = 0;
            long now = System.currentTimeMillis();
            entity.createdAt = now;
            entity.updatedAt = now;
            viewModel.saveVehicle(entity, () -> toast("Đã thêm phương tiện."));
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void showChargeForm() {
        if (vehicles.isEmpty()) { toast("Hãy thêm phương tiện trước."); return; }
        DialogBatteryChargeBinding form = DialogBatteryChargeBinding.inflate(getLayoutInflater());
        List<String> names = new ArrayList<>();
        for (VehicleEntity vehicle : vehicles) names.add(vehicle.name);
        form.dropdownChargeVehicle.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, names));
        form.dropdownChargeVehicle.setText(names.get(0), false);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Ghi lần sạc")
                .setView(form.getRoot())
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener(x -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            int index = names.indexOf(form.dropdownChargeVehicle.getText().toString());
            if (index < 0) index = 0;
            int start = integer(form.edtChargeStart.getText());
            int end = integer(form.edtChargeEnd.getText());
            if (start < 0 || end > 100 || end <= start) { toast("Phần trăm pin không hợp lệ."); return; }
            BatteryChargeEntity entity = new BatteryChargeEntity();
            entity.vehicleId = vehicles.get(index).id;
            entity.chargedAt = System.currentTimeMillis();
            entity.startPercent = start;
            entity.endPercent = end;
            entity.energyKwh = decimal(form.edtChargeKwh.getText());
            entity.cost = decimal(form.edtChargeCost.getText());
            entity.odometerKm = decimal(form.edtChargeOdometer.getText());
            viewModel.saveCharge(entity, () -> toast("Đã lưu lần sạc."));
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void showFuelForm() {
        if (vehicles.isEmpty()) { toast("Hãy thêm phương tiện trước."); return; }
        DialogFuelLogBinding form = DialogFuelLogBinding.inflate(getLayoutInflater());
        List<String> names = new ArrayList<>();
        for (VehicleEntity vehicle : vehicles) names.add(vehicle.name);
        form.dropdownFuelVehicle.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, names));
        form.dropdownFuelVehicle.setText(names.get(0), false);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Ghi lần đổ nhiên liệu")
                .setView(form.getRoot())
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener(x -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            int index = names.indexOf(form.dropdownFuelVehicle.getText().toString());
            if (index < 0) index = 0;
            FuelLogEntity entity = new FuelLogEntity();
            entity.vehicleId = vehicles.get(index).id;
            entity.fueledAt = System.currentTimeMillis();
            entity.liters = decimal(form.edtFuelLiters.getText());
            entity.cost = decimal(form.edtFuelCost.getText());
            entity.odometerKm = decimal(form.edtFuelOdometer.getText());
            entity.station = text(form.edtFuelStation.getText());
            entity.fullTank = form.switchFullTank.isChecked();
            if (entity.liters <= 0 && entity.cost <= 0) { toast("Hãy nhập số lít hoặc chi phí."); return; }
            viewModel.saveFuel(entity, () -> toast("Đã lưu lần đổ nhiên liệu."));
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void showMaintenanceForm() {
        if (vehicles.isEmpty()) { toast("Hãy thêm phương tiện trước."); return; }
        DialogMaintenanceRecordBinding form = DialogMaintenanceRecordBinding.inflate(getLayoutInflater());
        List<String> names = new ArrayList<>();
        for (VehicleEntity vehicle : vehicles) names.add(vehicle.name);
        form.dropdownMaintenanceVehicle.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, names));
        form.dropdownMaintenanceVehicle.setText(names.get(0), false);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Thêm bảo dưỡng / kiểm tra")
                .setView(form.getRoot())
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener(x -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String title = text(form.edtMaintenanceTitle.getText());
            if (title.isEmpty()) { toast("Hãy nhập hạng mục bảo dưỡng."); return; }
            int index = names.indexOf(form.dropdownMaintenanceVehicle.getText().toString());
            if (index < 0) index = 0;
            MaintenanceRecordEntity entity = new MaintenanceRecordEntity();
            entity.vehicleId = vehicles.get(index).id;
            entity.itemType = title.toUpperCase(Locale.ROOT).replace(' ', '_');
            entity.title = title;
            entity.performedAt = System.currentTimeMillis();
            entity.odometerKm = decimal(form.edtMaintenanceOdometer.getText());
            entity.cost = decimal(form.edtMaintenanceCost.getText());
            int days = integer(form.edtMaintenanceDays.getText());
            entity.nextDueAt = days <= 0 ? 0 : System.currentTimeMillis() + days * 86_400_000L;
            entity.nextDueOdometerKm = decimal(form.edtMaintenanceNextKm.getText());
            entity.note = text(form.edtMaintenanceNote.getText());
            entity.condition = "RECORDED";
            entity.completed = false;
            viewModel.saveMaintenance(entity, () -> toast("Đã lưu lịch bảo dưỡng."));
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void confirmDeleteShift(DriverShiftEntity shift) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa ca chạy?")
                .setMessage(DateUtils.formatCompactDateKey(shift.dateKey) + " · " + MoneyUtils.format(ShiftProfitCalculator.netProfit(shift)))
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> viewModel.deleteShift(shift, () -> toast("Đã xóa ca chạy.")))
                .show();
    }

    private MaterialCardView recordCard(String titleText, String detailsText) {
        MaterialCardView card = new MaterialCardView(requireContext());
        card.setCardBackgroundColor(getResources().getColor(R.color.surface, requireContext().getTheme()));
        card.setStrokeColor(getResources().getColor(R.color.outline, requireContext().getTheme()));
        card.setStrokeWidth(dp(1));
        card.setRadius(dp(18));
        card.setCardElevation(0);
        card.setClickable(true);
        card.setFocusable(true);
        LinearLayout column = new LinearLayout(requireContext());
        column.setOrientation(LinearLayout.VERTICAL);
        column.setPadding(dp(15), dp(14), dp(15), dp(14));
        TextView title = new TextView(requireContext());
        title.setText(titleText);
        title.setTextColor(getResources().getColor(R.color.on_surface, requireContext().getTheme()));
        title.setTextSize(16);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        TextView details = new TextView(requireContext());
        details.setText(detailsText);
        details.setTextColor(getResources().getColor(R.color.on_surface_variant, requireContext().getTheme()));
        details.setTextSize(13);
        details.setPadding(0, dp(6), 0, 0);
        column.addView(title);
        column.addView(details);
        card.addView(column);
        return card;
    }

    private TextView emptyText(String message) {
        TextView text = new TextView(requireContext());
        text.setText(message);
        text.setGravity(Gravity.CENTER);
        text.setPadding(dp(18), dp(24), dp(18), dp(24));
        text.setTextColor(getResources().getColor(R.color.on_surface_variant, requireContext().getTheme()));
        return text;
    }

    private LinearLayout.LayoutParams cardParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(9);
        return params;
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private String text(CharSequence value) { return value == null ? "" : value.toString().trim(); }
    private boolean isBlank(String value) { return value == null || value.trim().isEmpty(); }
    private String safe(String value, String fallback) { return isBlank(value) ? fallback : value; }
    private double decimal(CharSequence value) { try { String s = text(value).replace(',', '.'); return s.isEmpty() ? 0 : Double.parseDouble(s); } catch (Exception e) { return 0; } }
    private int integer(CharSequence value) { try { String s = text(value); return s.isEmpty() ? 0 : Integer.parseInt(s); } catch (Exception e) { return 0; } }
    private String number(double value) { return value == 0 ? "" : String.format(Locale.US, "%.2f", value).replaceAll("\\.?0+$", ""); }
    private String time(long timestamp) { return new java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(new java.util.Date(timestamp)); }
    private long parseDateTime(String dateKey, String hhmm) {
        String[] pieces = hhmm.split(":");
        if (pieces.length != 2) throw new IllegalArgumentException("time");
        return DateUtils.atTime(dateKey, Integer.parseInt(pieces[0]), Integer.parseInt(pieces[1]));
    }
    private void toast(String message) { Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show(); }

    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
