package com.sangapp.gooddaily.feature.metaphysics.ui.maihoa;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.databinding.FragmentMaiHoaCastBinding;
import com.sangapp.gooddaily.feature.metaphysics.data.DivinationSessionEntity;
import com.sangapp.gooddaily.feature.metaphysics.domain.DivinationResult;
import com.sangapp.gooddaily.feature.metaphysics.domain.FiveElement;
import com.sangapp.gooddaily.feature.metaphysics.domain.MaiHoaCalculator;
import com.sangapp.gooddaily.feature.metaphysics.domain.QuestionTopic;
import com.sangapp.gooddaily.feature.metaphysics.ui.MetaphysicsViewModel;
import com.sangapp.gooddaily.util.DateUtils;
import com.sangapp.gooddaily.util.LunarCalendarUtils;
import com.sangapp.gooddaily.util.ThemeUtils;

import java.util.Calendar;

public class MaiHoaCastFragment extends Fragment {
    private FragmentMaiHoaCastBinding binding;
    private MetaphysicsViewModel viewModel;
    private DivinationResult currentResult;
    private String currentMethod = "NUMBER";
    private String currentInput = "";
    private QuestionTopic currentTopic = QuestionTopic.GENERAL;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMaiHoaCastBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(MetaphysicsViewModel.class);
        ArrayAdapter<QuestionTopic> topicAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, QuestionTopic.values());
        topicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerMaiHoaTopic.setAdapter(topicAdapter);
        binding.btnMaiHoaFromNumbers.setOnClickListener(v -> calculateNumbers());
        binding.btnMaiHoaFromTime.setOnClickListener(v -> calculateTime());
        binding.btnSaveMaiHoa.setOnClickListener(v -> save());
    }

    private void calculateNumbers() {
        try {
            long upper = parseLong(text(binding.edtMaiHoaUpper.getText()));
            long lower = parseLong(text(binding.edtMaiHoaLower.getText()));
            long moving = parseLong(text(binding.edtMaiHoaMoving.getText()));
            currentTopic = selectedTopic();
            String question = text(binding.edtMaiHoaQuestion.getText());
            if (currentTopic == QuestionTopic.GENERAL) currentTopic = QuestionTopic.infer(question);
            currentResult = MaiHoaCalculator.fromNumbers(upper, lower, moving,
                    currentTopic, question, Calendar.getInstance());
            currentMethod = "NUMBER";
            currentInput = upper + "," + lower + "," + moving;
            render();
        } catch (Exception e) {
            toast("Hãy nhập đủ ba số hợp lệ.");
        }
    }

    private void calculateTime() {
        Calendar now = Calendar.getInstance();
        currentTopic = selectedTopic();
        String question = text(binding.edtMaiHoaQuestion.getText());
        if (currentTopic == QuestionTopic.GENERAL) currentTopic = QuestionTopic.infer(question);
        currentResult = MaiHoaCalculator.fromDateTime(now, currentTopic, question);
        currentMethod = "TIME";
        currentInput = String.valueOf(now.getTimeInMillis());
        render();
    }

    private QuestionTopic selectedTopic() {
        Object item = binding.spinnerMaiHoaTopic.getSelectedItem();
        return item instanceof QuestionTopic ? (QuestionTopic) item : QuestionTopic.GENERAL;
    }

    private void render() {
        if (currentResult == null) return;
        binding.cardMaiHoaResult.setVisibility(View.VISIBLE);
        binding.tvMaiHoaBase.setText(currentResult.base.title() + " · " + currentResult.base.symbols());
        binding.hexagramMaiHoaBase.setHexagram(currentResult.base.linesBottomUp, currentResult.movingLines);
        binding.hexagramMaiHoaChanged.setHexagram(currentResult.changed.linesBottomUp, new int[0]);
        binding.tvMaiHoaChanged.setText(currentResult.movingLinesText()
                + "\nQuẻ hỗ: " + currentResult.nuclear.title()
                + "\nQuẻ biến: " + currentResult.changed.title());
        binding.tvMaiHoaBodyUse.setText(currentResult.bodyUse + "\n\n" + currentResult.elementRelation);
        binding.tvMaiHoaInterpretation.setText(currentResult.interpretation);
        binding.tvMaiHoaTechnical.setText(currentResult.technicalDetails);
        binding.tvMaiHoaTiming.setText(currentResult.timing);
        binding.tvMaiHoaConfidence.setText(currentResult.confidence);
        renderMetaChips();
    }

    private void renderMetaChips() {
        binding.chipMaiHoaMeta.removeAllViews();
        addChip(currentTopic.toString(), R.color.primary);
        addElementChip("Thượng · " + currentResult.base.upper.element.vietnamese,
                currentResult.base.upper.element);
        addElementChip("Hạ · " + currentResult.base.lower.element.vietnamese,
                currentResult.base.lower.element);
        addChip(currentResult.movingLinesText(), R.color.hexagram_moving_line);
    }

    private void addElementChip(String text, FiveElement element) {
        @ColorRes int color;
        switch (element) {
            case WOOD: color = R.color.element_wood; break;
            case FIRE: color = R.color.element_fire; break;
            case EARTH: color = R.color.element_earth; break;
            case METAL: color = R.color.element_metal; break;
            case WATER:
            default: color = R.color.element_water; break;
        }
        addChip(text, color);
    }

    private void addChip(String text, @ColorRes int colorRes) {
        int color = ContextCompat.getColor(requireContext(), colorRes);
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setTextColor(color);
        chip.setChipBackgroundColor(ColorStateList.valueOf(ThemeUtils.adjustAlpha(color, 0.16f)));
        chip.setChipStrokeColor(ColorStateList.valueOf(ThemeUtils.adjustAlpha(color, 0.50f)));
        chip.setChipStrokeWidth(getResources().getDisplayMetrics().density);
        chip.setClickable(false);
        chip.setCheckable(false);
        binding.chipMaiHoaMeta.addView(chip);
    }

    private void save() {
        if (currentResult == null) {
            toast("Bạn cần lập quẻ trước khi lưu.");
            return;
        }
        long now = System.currentTimeMillis();
        DivinationSessionEntity entity = new DivinationSessionEntity();
        entity.system = "MAI_HOA";
        entity.method = currentMethod + "|" + currentTopic.name();
        entity.question = text(binding.edtMaiHoaQuestion.getText());
        if (entity.question.isEmpty()) entity.question = "Mai Hoa " + DateUtils.formatDateTime(now);
        entity.castTime = now;
        entity.lunarText = LunarCalendarUtils.formatLunar(DateUtils.dateKey(now));
        entity.inputData = currentInput;
        fillResult(entity, currentResult);
        entity.status = "CHO_KIEM_CHUNG";
        entity.createdAt = now;
        entity.updatedAt = now;
        viewModel.save(entity, () -> toast("Đã lưu quẻ Mai Hoa và toàn bộ luận giải."));
    }

    private static void fillResult(DivinationSessionEntity entity, DivinationResult result) {
        entity.baseHexagramNumber = result.base.number;
        entity.baseHexagramName = result.base.name;
        entity.changedHexagramNumber = result.changed.number;
        entity.changedHexagramName = result.changed.name;
        entity.nuclearHexagramNumber = result.nuclear.number;
        entity.nuclearHexagramName = result.nuclear.name;
        entity.movingLines = result.movingLinesText();
        entity.bodyUse = result.bodyUse;
        entity.elementRelation = result.elementRelation;
        entity.interpretation = result.fullInterpretation();
    }

    private long parseLong(String value) {
        if (value.isEmpty()) throw new IllegalArgumentException("empty");
        return Long.parseLong(value);
    }

    private String text(CharSequence value) { return value == null ? "" : value.toString().trim(); }
    private void toast(String message) { Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show(); }

    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
