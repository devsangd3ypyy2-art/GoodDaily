package com.sangapp.gooddaily.feature.metaphysics.ui.history;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sangapp.gooddaily.databinding.DialogDivinationVerificationBinding;
import com.sangapp.gooddaily.databinding.FragmentDivinationHistoryBinding;
import com.sangapp.gooddaily.feature.metaphysics.data.DivinationSessionEntity;
import com.sangapp.gooddaily.feature.metaphysics.ui.MetaphysicsViewModel;

import java.util.List;

public class DivinationHistoryFragment extends Fragment {
    private FragmentDivinationHistoryBinding binding;
    private MetaphysicsViewModel viewModel;
    private DivinationSessionAdapter adapter;
    private LiveData<List<DivinationSessionEntity>> currentSource;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDivinationHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(MetaphysicsViewModel.class);
        adapter = new DivinationSessionAdapter(new DivinationSessionAdapter.Listener() {
            @Override public void onClick(DivinationSessionEntity item) { showDetails(item); }
            @Override public void onLongClick(DivinationSessionEntity item) { confirmDelete(item); }
        });
        binding.recyclerDivinationHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerDivinationHistory.setAdapter(adapter);
        observe(viewModel.sessions());
        binding.edtDivinationSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { observe(viewModel.search(s == null ? "" : s.toString())); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void observe(LiveData<List<DivinationSessionEntity>> source) {
        if (currentSource != null) currentSource.removeObservers(getViewLifecycleOwner());
        currentSource = source;
        currentSource.observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
            boolean empty = items == null || items.isEmpty();
            binding.tvDivinationEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.recyclerDivinationHistory.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    private void showDetails(DivinationSessionEntity item) {
        DialogDivinationVerificationBinding form = DialogDivinationVerificationBinding.inflate(getLayoutInflater());
        form.tvVerificationSummary.setText(
                safe(item.question) + "\n\nQuẻ chủ: " + safe(item.baseHexagramName)
                        + "\nQuẻ hỗ: " + safe(item.nuclearHexagramName)
                        + "\nQuẻ biến: " + safe(item.changedHexagramName)
                        + "\n" + safe(item.movingLines)
                        + "\n\n" + safe(item.interpretation));
        form.edtVerification.setText(item.verification);
        form.switchVerified.setChecked("DA_KIEM_CHUNG".equals(item.status));
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Chi tiết và nghiệm lý")
                .setView(form.getRoot())
                .setNegativeButton("Đóng", null)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    item.verification = form.edtVerification.getText() == null ? "" : form.edtVerification.getText().toString().trim();
                    item.status = form.switchVerified.isChecked() ? "DA_KIEM_CHUNG" : "CHO_KIEM_CHUNG";
                    item.verifiedAt = form.switchVerified.isChecked() ? System.currentTimeMillis() : 0;
                    item.updatedAt = System.currentTimeMillis();
                    viewModel.save(item, () -> toast("Đã cập nhật nghiệm lý."));
                }).show();
    }

    private void confirmDelete(DivinationSessionEntity item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xóa quẻ đã lưu?")
                .setMessage(safe(item.question))
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> viewModel.delete(item, () -> toast("Đã xóa.")))
                .show();
    }

    private String safe(String value) { return value == null ? "" : value; }
    private void toast(String message) { Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show(); }

    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
