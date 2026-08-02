package com.sangapp.gooddaily.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.sangapp.gooddaily.data.local.prefs.LocalUserStore;
import com.sangapp.gooddaily.data.local.prefs.SecureStore;
import com.sangapp.gooddaily.databinding.ActivityPinLockBinding;
import com.sangapp.gooddaily.util.SecuritySession;

import java.util.concurrent.Executor;

public class PinLockActivity extends AppCompatActivity {
    public static final String EXTRA_MODULE = "unlock_module";
    private ActivityPinLockBinding binding;
    private SecureStore secureStore;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPinLockBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        secureStore = new SecureStore(this);
        LocalUserStore userStore = new LocalUserStore(this);
        binding.btnUnlockBiometric.setVisibility(userStore.isBiometricEnabled() ? View.VISIBLE : View.GONE);
        binding.btnUnlockWithPin.setOnClickListener(v -> {
            String pin = binding.edtUnlockPin.getText() == null ? "" : binding.edtUnlockPin.getText().toString();
            if (secureStore.verifyPin(pin)) unlock();
            else Toast.makeText(this, "Mã PIN không đúng.", Toast.LENGTH_SHORT).show();
        });
        binding.btnUnlockBiometric.setOnClickListener(v -> showBiometric());
        if (userStore.isBiometricEnabled()) showBiometric();
    }

    private void showBiometric() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt prompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) { super.onAuthenticationSucceeded(result); unlock(); }
            @Override public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) { super.onAuthenticationError(errorCode, errString); }
        });
        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Mở khóa Good Daily")
                .setSubtitle("Xác thực bằng sinh trắc học")
                .setNegativeButtonText("Dùng mã PIN")
                .build();
        prompt.authenticate(info);
    }

    private void unlock() {
        String module = getIntent().getStringExtra(EXTRA_MODULE);
        if (module == null || module.trim().isEmpty()) SecuritySession.markUnlocked();
        else SecuritySession.markModuleUnlocked(module);
        setResult(RESULT_OK);
        finish();
    }

    @Override public void onBackPressed() { moveTaskToBack(true); }
}
