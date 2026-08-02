package com.sangapp.gooddaily.ui.security;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.data.local.prefs.SecureStore;
import com.sangapp.gooddaily.databinding.FragmentSecurityCenterBinding;
import com.sangapp.gooddaily.ui.auth.PinLockActivity;
import com.sangapp.gooddaily.util.SecuritySession;
import com.sangapp.gooddaily.util.ThemeUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class SecurityCenterFragment extends Fragment {
    private FragmentSecurityCenterBinding binding;
    private LocalUserStore store;
    private SecureStore secureStore;
    private final Map<String,Integer> lockOptions = new LinkedHashMap<>();

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSecurityCenterBinding.inflate(inflater, container, false); return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        store = new LocalUserStore(requireContext()); secureStore = new SecureStore(requireContext());
        lockOptions.put("Không tự khóa",0); lockOptions.put("Sau 1 phút",1); lockOptions.put("Sau 5 phút",5); lockOptions.put("Sau 15 phút",15); lockOptions.put("Sau 30 phút",30);
        binding.dropdownAutoLock.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, lockOptions.keySet().toArray(new String[0])));
        binding.dropdownAutoLock.setText(labelForMinutes(store.getAutoLockMinutes()), false);
        binding.switchBiometric.setChecked(store.isBiometricEnabled());
        binding.switchSecureScreen.setChecked(store.isSecureScreenEnabled());
        binding.switchFinanceLock.setChecked(store.isFinanceLockEnabled());
        binding.switchJournalLock.setChecked(store.isJournalLockEnabled());
        renderPinStatus();
        ThemeUtils.tintFilledButton(binding.btnSetPin, requireContext(), store.getThemeKey());
        ThemeUtils.tintFilledButton(binding.btnLockNow, requireContext(), store.getThemeKey());
        ThemeUtils.tintTonalButton(binding.btnDisablePin, requireContext(), store.getThemeKey());

        binding.btnSetPin.setOnClickListener(v -> showSetPinDialog());
        binding.btnDisablePin.setOnClickListener(v -> showDisablePinDialog());
        binding.switchBiometric.setOnCheckedChangeListener((buttonView, checked) -> {
            if (checked) {
                if (!secureStore.hasPin()) { buttonView.setChecked(false); toast("Hãy đặt PIN trước."); return; }
                int result = BiometricManager.from(requireContext()).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK);
                if (result != BiometricManager.BIOMETRIC_SUCCESS) { buttonView.setChecked(false); toast("Thiết bị chưa sẵn sàng cho sinh trắc học."); return; }
            }
            store.setBiometricEnabled(checked);
        });
        binding.dropdownAutoLock.setOnItemClickListener((parent, v, position, id) -> {
            Integer value = lockOptions.get(parent.getItemAtPosition(position).toString());
            store.setAutoLockMinutes(value == null ? 5 : value);
        });
        binding.switchSecureScreen.setOnCheckedChangeListener((buttonView, checked) -> { store.setSecureScreenEnabled(checked); requireActivity().recreate(); });
        binding.switchFinanceLock.setOnCheckedChangeListener((buttonView, checked) -> store.setFinanceLockEnabled(checked));
        binding.switchJournalLock.setOnCheckedChangeListener((buttonView, checked) -> store.setJournalLockEnabled(checked));
        binding.btnLockNow.setOnClickListener(v -> {
            if (!secureStore.hasPin()) { toast("Hãy đặt PIN trước."); return; }
            SecuritySession.lockNow();
            startActivity(new Intent(requireContext(), PinLockActivity.class));
        });
    }

    private void showSetPinDialog() {
        LinearLayout form = new LinearLayout(requireContext()); form.setOrientation(LinearLayout.VERTICAL); int pad=dp(4); form.setPadding(pad,pad,pad,0);
        EditText first = pinField("PIN mới (4–8 số)"); EditText second = pinField("Nhập lại PIN");
        form.addView(first); form.addView(second);
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext()).setTitle(secureStore.hasPin()?"Đổi mã PIN":"Đặt mã PIN").setView(form).setNegativeButton("Hủy",null).setPositiveButton("Lưu",null).create();
        dialog.setOnShowListener(x -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String a=first.getText().toString().trim(), b=second.getText().toString().trim();
            if (!a.matches("\\d{4,8}")) { first.setError("PIN cần 4–8 chữ số"); return; }
            if (!a.equals(b)) { second.setError("PIN nhập lại chưa khớp"); return; }
            secureStore.setPin(a); SecuritySession.markUnlocked(); renderPinStatus(); dialog.dismiss(); toast("Đã lưu mã PIN.");
        }));
        dialog.show();
    }

    private void showDisablePinDialog() {
        if (!secureStore.hasPin()) { toast("PIN chưa được bật."); return; }
        EditText input = pinField("Nhập PIN hiện tại");
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext()).setTitle("Tắt mã PIN?").setView(input).setNegativeButton("Hủy",null).setPositiveButton("Tắt",null).create();
        dialog.setOnShowListener(x -> dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (!secureStore.verifyPin(input.getText().toString())) { input.setError("PIN không đúng"); return; }
            secureStore.clearPin(); store.setBiometricEnabled(false); binding.switchBiometric.setChecked(false); SecuritySession.markUnlocked(); renderPinStatus(); dialog.dismiss();
        }));
        dialog.show();
    }

    private EditText pinField(String hint) { EditText e=new EditText(requireContext());e.setHint(hint);e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);e.setMaxLines(1);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);p.bottomMargin=dp(8);e.setLayoutParams(p);return e; }
    private void renderPinStatus(){ if(binding!=null) binding.tvPinStatus.setText(secureStore.hasPin()?"PIN đang bật. Dữ liệu PIN được bảo vệ bởi Android Keystore.":"PIN chưa được bật."); }
    private String labelForMinutes(int minutes){for(Map.Entry<String,Integer> e:lockOptions.entrySet())if(e.getValue()==minutes)return e.getKey();return "Sau 5 phút";}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    private void toast(String m){Toast.makeText(requireContext(),m,Toast.LENGTH_LONG).show();}
    @Override public void onDestroyView(){super.onDestroyView();binding=null;}
}
