package com.sangapp.gooddaily.feature.metaphysics.ui.liuhao;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.ColorStateList;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.sangapp.gooddaily.R;
import com.sangapp.gooddaily.databinding.FragmentLiuHaoCastBinding;
import com.sangapp.gooddaily.feature.metaphysics.data.DivinationSessionEntity;
import com.sangapp.gooddaily.feature.metaphysics.domain.DivinationResult;
import com.sangapp.gooddaily.feature.metaphysics.domain.EarthlyBranch;
import com.sangapp.gooddaily.feature.metaphysics.domain.FiveElement;
import com.sangapp.gooddaily.feature.metaphysics.domain.GanzhiDate;
import com.sangapp.gooddaily.feature.metaphysics.domain.HeavenlyStem;
import com.sangapp.gooddaily.feature.metaphysics.domain.LiuHaoCalculator;
import com.sangapp.gooddaily.feature.metaphysics.domain.LiuHaoContext;
import com.sangapp.gooddaily.feature.metaphysics.domain.QuestionTopic;
import com.sangapp.gooddaily.feature.metaphysics.ui.DivinationUi;
import com.sangapp.gooddaily.feature.metaphysics.ui.MetaphysicsViewModel;
import com.sangapp.gooddaily.util.DateUtils;
import com.sangapp.gooddaily.util.LunarCalendarUtils;
import com.sangapp.gooddaily.util.ThemeUtils;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Calendar;

public class LiuHaoCastFragment extends Fragment {
    private static final SecureRandom RANDOM = new SecureRandom();

    private FragmentLiuHaoCastBinding binding;
    private MetaphysicsViewModel viewModel;
    private DivinationResult currentResult;
    private QuestionTopic currentTopic = QuestionTopic.GENERAL;
    private LiuHaoContext currentContext;
    private final int[] ritualValues = new int[6];
    private int nextRitualLine;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLiuHaoCastBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(MetaphysicsViewModel.class);
        setupSpinners();
        resetRitual();
        binding.btnCastCoins.setOnClickListener(v -> castNextLine());
        binding.btnResetRitual.setOnClickListener(v -> resetRitual());
        binding.btnCalculateLiuHao.setOnClickListener(v -> calculateManual());
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

    private void castNextLine() {
        if (nextRitualLine >= 6) return;
        int[] coins = new int[]{coinValue(), coinValue(), coinValue()};
        int value = coins[0] + coins[1] + coins[2];
        ritualValues[nextRitualLine] = value;
        fields()[nextRitualLine].setText(String.valueOf(value));
        updateCoinFaces(coins);
        animateCoins();
        playCoinSoundAndHaptic();
        nextRitualLine++;
        updateRitualUi();

        if (nextRitualLine == 6) {
            currentContext = buildContext();
            currentResult = LiuHaoCalculator.calculate(Arrays.copyOf(ritualValues, 6), currentContext);
            render();
            toast("Đã đủ sáu hào. Kết quả được lập từ Hào 1 dưới cùng đến Hào 6 trên cùng.");
        }
    }

    private int coinValue() {
        return RANDOM.nextBoolean() ? 3 : 2;
    }

    private void updateCoinFaces(int[] coins) {
        TextView[] views = new TextView[]{binding.coin1, binding.coin2, binding.coin3};
        for (int i = 0; i < views.length; i++) {
            views[i].setText(coins[i] == 3 ? "D" : "Â");
            views[i].setContentDescription(coins[i] == 3 ? "Mặt dương, ba điểm" : "Mặt âm, hai điểm");
        }
    }

    private void animateCoins() {
        TextView[] coins = new TextView[]{binding.coin1, binding.coin2, binding.coin3};
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator[] animators = new ObjectAnimator[coins.length * 3];
        int index = 0;
        for (int i = 0; i < coins.length; i++) {
            TextView coin = coins[i];
            long delay = i * 45L;
            ObjectAnimator rotate = ObjectAnimator.ofFloat(coin, View.ROTATION_Y, 0f, 720f);
            rotate.setDuration(560L);
            rotate.setStartDelay(delay);
            ObjectAnimator jump = ObjectAnimator.ofFloat(coin, View.TRANSLATION_Y, 0f, -34f, 0f, -8f, 0f);
            jump.setDuration(600L);
            jump.setStartDelay(delay);
            ObjectAnimator scale = ObjectAnimator.ofFloat(coin, View.SCALE_X, 1f, 0.82f, 1f);
            scale.setDuration(560L);
            scale.setStartDelay(delay);
            animators[index++] = rotate;
            animators[index++] = jump;
            animators[index++] = scale;
        }
        set.playTogether(animators);
        set.start();
    }

    private void playCoinSoundAndHaptic() {
        binding.btnCastCoins.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        MediaPlayer player = MediaPlayer.create(requireContext(), R.raw.coin_drop);
        if (player != null) {
            player.setOnCompletionListener(MediaPlayer::release);
            player.setOnErrorListener((mp, what, extra) -> {
                mp.release();
                return true;
            });
            player.start();
        }
    }

    private void resetRitual() {
        Arrays.fill(ritualValues, 0);
        nextRitualLine = 0;
        currentResult = null;
        currentContext = null;
        for (TextInputEditText field : fields()) field.setText("");
        if (binding != null) {
            binding.ritualHexagram.clearLines();
            binding.ritualProgress.setProgressCompat(0, false);
            binding.cardLiuHaoResult.setVisibility(View.GONE);
            binding.btnCastCoins.setEnabled(true);
            binding.btnCastCoins.setText("Gieo Hào 1");
            binding.tvRitualStep.setText("Sẵn sàng gieo Hào 1 · hào dưới cùng");
            binding.coin1.setText("D");
            binding.coin2.setText("Â");
            binding.coin3.setText("D");
        }
    }

    private void updateRitualUi() {
        binding.ritualHexagram.setLineValues(ritualValues, false);
        binding.ritualProgress.setProgressCompat(nextRitualLine, true);
        if (nextRitualLine >= 6) {
            binding.tvRitualStep.setText("Hoàn tất 6/6 hào · đang hiển thị quẻ chủ và quẻ biến");
            binding.btnCastCoins.setText("Đã đủ 6 hào");
            binding.btnCastCoins.setEnabled(false);
        } else {
            int line = nextRitualLine + 1;
            binding.tvRitualStep.setText("Đã gieo " + nextRitualLine + "/6 · tiếp theo Hào " + line
                    + (line <= 3 ? " thuộc nội quái" : " thuộc ngoại quái"));
            binding.btnCastCoins.setText("Gieo Hào " + line);
        }
    }

    private void calculateManual() {
        try {
            int[] values = new int[6];
            TextInputEditText[] fields = fields();
            for (int i = 0; i < fields.length; i++) {
                String value = fields[i].getText() == null ? "" : fields[i].getText().toString().trim();
                values[i] = Integer.parseInt(value);
            }
            System.arraycopy(values, 0, ritualValues, 0, 6);
            nextRitualLine = 6;
            updateRitualUi();
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
        binding.tvLiuHaoBase.setText(currentResult.base.title() + " · " + currentResult.base.symbols());
        binding.hexagramLiuHaoBase.setLineValues(currentResult.lineValues, false);
        binding.hexagramLiuHaoChanged.setLineValues(currentResult.lineValues, true);
        binding.tvLiuHaoChanged.setText(currentResult.movingLinesText()
                + "\nQuẻ hỗ: " + currentResult.nuclear.title()
                + "\nQuẻ biến: " + currentResult.changed.title());
        binding.tvLiuHaoInterpretation.setText(currentResult.interpretation);
        binding.tvLiuHaoTechnical.setText(currentResult.technicalDetails);
        binding.tvLiuHaoTiming.setText(currentResult.timing);
        binding.tvLiuHaoConfidence.setText(currentResult.confidence);
        renderMetaChips();
        binding.cardLiuHaoResult.post(() -> binding.cardLiuHaoResult.requestFocus());
    }

    private void renderMetaChips() {
        binding.chipLiuHaoMeta.removeAllViews();
        addChip(currentTopic.toString(), R.color.primary);
        addElementChip("Nội · " + currentResult.base.lower.element.vietnamese,
                currentResult.base.lower.element);
        addElementChip("Ngoại · " + currentResult.base.upper.element.vietnamese,
                currentResult.base.upper.element);
        addChip(currentResult.movingLines.length == 0 ? "Tĩnh quẻ" : currentResult.movingLines.length + " hào động",
                currentResult.movingLines.length == 0 ? R.color.secondary : R.color.hexagram_moving_line);
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
        binding.chipLiuHaoMeta.addView(chip);
    }

    private void save() {
        if (currentResult == null || currentContext == null) {
            toast("Bạn cần lập quẻ trước khi lưu.");
            return;
        }
        long now = System.currentTimeMillis();
        DivinationSessionEntity entity = new DivinationSessionEntity();
        entity.system = "LIU_HAO";
        entity.method = "THREE_COINS_RITUAL|" + currentTopic.name();
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
