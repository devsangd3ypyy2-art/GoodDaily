package com.sangapp.gooddaily.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.color.DynamicColors;

import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.databinding.ActivityAuthBinding;
import com.sangapp.gooddaily.ui.MainActivity;

public class AuthActivity extends AppCompatActivity {
    private ActivityAuthBinding binding;
    private LocalUserStore userStore;
    private boolean registerMode;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userStore = new LocalUserStore(this);
        if (userStore.isDynamicColorsEnabled()) DynamicColors.applyToActivityIfAvailable(this);
        if (userStore.isLoggedIn()) {
            openMain();
            return;
        }

        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        registerMode = !userStore.hasAccount();
        renderMode();

        binding.tvSwitchMode.setOnClickListener(v -> {
            registerMode = !registerMode;
            renderMode();
        });
        binding.btnSubmit.setOnClickListener(v -> submit());
    }

    private void renderMode() {
        binding.layoutDisplayName.setVisibility(registerMode ? View.VISIBLE : View.GONE);
        binding.btnSubmit.setText(registerMode ? "Tạo tài khoản" : "Đăng nhập");
        binding.tvSwitchMode.setText(registerMode ? "Đã có tài khoản? Đăng nhập" : "Chưa có tài khoản? Tạo tài khoản");
        binding.edtUsername.setText(registerMode ? "" : userStore.getUsername());
    }

    private void submit() {
        String username = text(binding.edtUsername.getText());
        String password = text(binding.edtPassword.getText());
        boolean success;
        if (registerMode) {
            success = userStore.register(text(binding.edtDisplayName.getText()), username, password);
            if (!success) Toast.makeText(this, "Nhập đủ tên, tài khoản và mật khẩu từ 4 ký tự.", Toast.LENGTH_SHORT).show();
        } else {
            success = userStore.login(username, password);
            if (!success) Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu không đúng.", Toast.LENGTH_SHORT).show();
        }
        if (success) openMain();
    }

    private String text(CharSequence value) { return value == null ? "" : value.toString().trim(); }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
