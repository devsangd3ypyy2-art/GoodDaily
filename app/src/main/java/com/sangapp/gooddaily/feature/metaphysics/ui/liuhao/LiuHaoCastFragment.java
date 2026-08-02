package com.sangapp.gooddaily.feature.metaphysics.ui.liuhao;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.sangapp.gooddaily.databinding.FragmentLiuHaoCastBinding;
import com.sangapp.gooddaily.feature.metaphysics.data.DivinationSessionEntity;
import com.sangapp.gooddaily.feature.metaphysics.domain.DivinationResult;
import com.sangapp.gooddaily.feature.metaphysics.domain.EarthlyBranch;
import com.sangapp.gooddaily.feature.metaphysics.domain.GanzhiDate;
import com.sangapp.gooddaily.feature.metaphysics.domain.HeavenlyStem;
import com.sangapp.gooddaily.feature.metaphysics.domain.LiuHaoCalculator;
import com.sangapp.gooddaily.feature.metaphysics.domain.LiuHaoContext;
import com.sangapp.gooddaily.feature.metaphysics.domain.QuestionTopic;
import com.sangapp.gooddaily.feature.metaphysics.ui.DivinationUi;
import com.sangapp.gooddaily.feature.metaphysics.ui.MetaphysicsViewModel;
import com.sangapp.gooddaily.util.DateUtils;
import com.sangapp.gooddaily.util.LunarCalendarUtils;

import java.util.Calendar;

public class LiuHaoCastFragment extends Fragment {
    private FragmentLiuHaoCastBinding binding;
    private MetaphysicsViewModel viewModel;
    private DivinationResult currentResult;
    private QuestionTopic currentTopic = QuestionTopic.GENERAL;
    private LiuHaoContext currentContext;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLiuHaoCastBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(MetaphysicsViewModel.class);
        setupSpinners();
        binding.btnCastCoins.setOnClickListener(v -> cast());
        binding.btnCalculateLiuHao.setOnClickListener(v -> calculate());
        binding.btnSaveLiuHao.setOnClickListener(v -> save());
    }

    private void setupSpinners() {
        setAdapter(binding.spinnerLiuHaoTopic, QuestionTopic.values());
        setAdapter(binding.spinnerMonthBranch, EarthlyBranch.values());
        setAdapter(binding.spinnerDayStem, HeavenlyStem.values());
        setAdapter(binding.spinnerDayBranch, EarthlyBranch.values());

        GanzhiDate current = GanzhiDate.approximate(Calendar.getInstance());
        binding.spinnerMonthBranch.setSelection(current.monthBranch.ordinal());
        binding.spinnerDayStem.setSelection(current.dayStem.ordinal());
        binding.spinnerDayBranch.setSelection(current.dayBranch.ordinal());
        binding.tvGanzhiHint.setText("Tự điền gần đúng: Nguyệt " + current.monthBranch
                + " · Nhật " + current.dayStem + current.dayBranch
                + ". Có thể chỉnh tay theo lịch Can Chi bạn sử dụng.");
    }

    private <T> void setAdapter(android.widget.Spinner spinner, T[] values) {
        ArrayAdapter<T> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void cast() {
        int[] values = LiuHaoCalculator.castSixLines();
        TextInputEditText[] fields = fields();
        for (int i = 0; i < fields.length; i++) fields[i].setText(String.valueOf(values[i]));
        currentContext = buildContext();
        currentResult = LiuHaoCalculator.calculate(values, currentContext);
        render();
    }

    private void calculate() {
        try {
            int[] values = new int[6];
            TextInputEditText[] fields = fields();
            for (int i = 0; i < fields.length; i++) {
                String value = fields[i].getText() == null ? "" : fields[i].getText().toString().trim();
                values[i] = Integer.parseInt(value);
            }
            currentContext = buildContext();
            currentResult = LiuHaoCalculator.calculate(values, currentContext);
            render();
        } catch (Exception e) {
            toast(e.getMessage() == null ? "Hãy nhập đủ 6 giá trị 6–9." : e.getMessage());
        }
    }

    private LiuHaoContext buildContext() {
        String question = text(binding.edtLiuHaoQuestion.getText());
        currentTopic = (QuestionTopic) binding.spinnerLiuHaoTopic.getSelectedItem();
        if (currentTopic == QuestionTopic.GENERAL) currentTopic = QuestionTopic.infer(question);
        return new LiuHaoContext(currentTopic, question,
                (HeavenlyStem) binding.spinnerDayStem.getSelectedItem(),
                (EarthlyBranch) binding.spinnerDayBranch.getSelectedItem(),
                (EarthlyBranch) binding.spinnerMonthBranch.getSelectedItem());
    }

    private void render() {
        if (currentResult == null) return;
        binding.cardLiuHaoResult.setVisibility(View.VISIBLE);
        binding.tvLiuHaoBase.setText(currentResult.base.title() + "\n" + currentResult.base.symbols());
        binding.tvLiuHaoLines.setText(DivinationUi.renderLines(currentResult));
        binding.tvLiuHaoChanged.setText(currentResult.movingLinesText()
                + "\nQuẻ hỗ: " + currentResult.nuclear.title()
                + "\nQuẻ biến: " + currentResult.changed.title());
        binding.tvLiuHaoInterpretation.setText(currentResult.interpretation);
        binding.tvLiuHaoTechnical.setText(currentResult.technicalDetails);
        binding.tvLiuHaoTiming.setText(currentResult.timing);
        binding.tvLiuHaoConfidence.setText(currentResult.confidence);
    }

    private void save() {
        if (currentResult == null || currentContext == null) {
            toast("Bạn cần lập quẻ trước khi lưu.");
            return;
        }
        long now = System.currentTimeMillis();
        DivinationSessionEntity entity = new DivinationSessionEntity();
        entity.system = "LIU_HAO";
        entity.method = "THREE_COINS|" + currentTopic.name();
        entity.question = text(binding.edtLiuHaoQuestion.getText());
        if (entity.question.isEmpty()) entity.question = "Lục Hào " + DateUtils.formatDateTime(now);
        entity.castTime = now;
        entity.lunarText = LunarCalendarUtils.formatLunar(DateUtils.dateKey(now));
        entity.inputData = DivinationUi.csv(currentResult.lineValues)
                + "|MONTH=" + currentContext.monthBranch.name()
                + "|DAY=" + currentContext.dayStem.name() + "_" + currentContext.dayBranch.name();
        entity.baseHexagramNumber = currentResult.base.number;
        entity.baseHexagramName = currentResult.base.name;
        entity.changedHexagramNumber = currentResult.changed.number;
        entity.changedHexagramName = currentResult.changed.name;
        entity.nuclearHexagramNumber = currentResult.nuclear.number;
        entity.nuclearHexagramName = currentResult.nuclear.name;
        entity.movingLines = currentResult.movingLinesText();
        entity.bodyUse = currentResult.bodyUse;
        entity.elementRelation = currentResult.elementRelation;
        entity.interpretation = currentResult.fullInterpretation();
        entity.status = "CHO_KIEM_CHUNG";
        entity.createdAt = now;
        entity.updatedAt = now;
        viewModel.save(entity, () -> toast("Đã lưu quẻ Lục Hào và toàn bộ bảng Nạp Giáp."));
    }

    private TextInputEditText[] fields() {
        return new TextInputEditText[]{binding.edtLine1, binding.edtLine2, binding.edtLine3,
                binding.edtLine4, binding.edtLine5, binding.edtLine6};
    }

    private String text(CharSequence value) { return value == null ? "" : value.toString().trim(); }
    private void toast(String message) { Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show(); }

    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
