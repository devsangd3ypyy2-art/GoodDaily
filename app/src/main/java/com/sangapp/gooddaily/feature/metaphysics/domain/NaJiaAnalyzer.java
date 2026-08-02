package com.sangapp.gooddaily.feature.metaphysics.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class NaJiaAnalyzer {
    private static final SixSpirit[] SPIRIT_ORDER = {
            SixSpirit.QING_LONG, SixSpirit.ZHU_QUE, SixSpirit.GOU_CHEN,
            SixSpirit.TENG_SHE, SixSpirit.BAI_HU, SixSpirit.XUAN_WU
    };

    private NaJiaAnalyzer() {}

    public static LiuHaoAnalysis analyze(HexagramInfo base, HexagramInfo changed,
                                         int[] lineValues, LiuHaoContext context) {
        EightPalaceResult palace = EightPalaceAnalyzer.analyze(base);
        EarthlyBranch[] baseBranches = branchesFor(base);
        HeavenlyStem[] baseStems = stemsFor(base);
        EarthlyBranch[] changedBranches = branchesFor(changed);
        Set<EarthlyBranch> voidBranches = calculateVoidBranches(context.dayStem, context.dayBranch);
        int spiritStart = spiritStart(context.dayStem);

        NaJiaLine[] lines = new NaJiaLine[6];
        for (int i = 0; i < 6; i++) {
            boolean moving = lineValues != null && lineValues.length == 6
                    && (lineValues[i] == 6 || lineValues[i] == 9);
            EarthlyBranch branch = baseBranches[i];
            SixRelative relative = relativeOf(palace.palaceElement, branch.element);
            SixSpirit spirit = SPIRIT_ORDER[(spiritStart + i) % 6];
            boolean isVoid = voidBranches.contains(branch);
            boolean monthBroken = branch.clashes(context.monthBranch);
            Strength strength = strengthOf(branch, context.monthBranch, context.dayBranch,
                    moving, isVoid, monthBroken);
            String notes = buildLineNotes(branch, context, moving, isVoid, monthBroken,
                    changedBranches[i]);
            lines[i] = new NaJiaLine(i + 1, base.linesBottomUp[i], moving,
                    baseStems[i], branch, relative, spirit,
                    i + 1 == palace.shiLine, i + 1 == palace.yingLine,
                    moving ? changedBranches[i] : null,
                    strength.score, isVoid, monthBroken, strength.label, notes);
        }

        TargetSelection target = selectTarget(context.topic, lines, palace);
        String judgment = buildJudgment(context, palace, lines, target.line);
        String timing = buildTiming(context, target.line);
        String confidence = confidence(lines, target.line);
        String technical = buildTechnical(context, palace, lines, target);
        return new LiuHaoAnalysis(palace, lines, target.line, target.reason,
                judgment, technical, timing, confidence);
    }

    private static EarthlyBranch[] branchesFor(HexagramInfo hexagram) {
        EarthlyBranch[] result = new EarthlyBranch[6];
        EarthlyBranch[] lower = innerBranches(hexagram.lower);
        EarthlyBranch[] upper = outerBranches(hexagram.upper);
        System.arraycopy(lower, 0, result, 0, 3);
        System.arraycopy(upper, 0, result, 3, 3);
        return result;
    }

    private static HeavenlyStem[] stemsFor(HexagramInfo hexagram) {
        HeavenlyStem[] result = new HeavenlyStem[6];
        HeavenlyStem lower = innerStem(hexagram.lower);
        HeavenlyStem upper = outerStem(hexagram.upper);
        for (int i = 0; i < 3; i++) result[i] = lower;
        for (int i = 3; i < 6; i++) result[i] = upper;
        return result;
    }

    private static EarthlyBranch[] innerBranches(Trigram trigram) {
        switch (trigram) {
            case QIAN: return new EarthlyBranch[]{EarthlyBranch.ZI, EarthlyBranch.YIN, EarthlyBranch.CHEN};
            case KUN: return new EarthlyBranch[]{EarthlyBranch.WEI, EarthlyBranch.SI, EarthlyBranch.MAO};
            case ZHEN: return new EarthlyBranch[]{EarthlyBranch.ZI, EarthlyBranch.YIN, EarthlyBranch.CHEN};
            case XUN: return new EarthlyBranch[]{EarthlyBranch.CHOU, EarthlyBranch.HAI, EarthlyBranch.YOU};
            case KAN: return new EarthlyBranch[]{EarthlyBranch.YIN, EarthlyBranch.CHEN, EarthlyBranch.WU};
            case LI: return new EarthlyBranch[]{EarthlyBranch.MAO, EarthlyBranch.CHOU, EarthlyBranch.HAI};
            case GEN: return new EarthlyBranch[]{EarthlyBranch.CHEN, EarthlyBranch.WU, EarthlyBranch.SHEN};
            case DUI: return new EarthlyBranch[]{EarthlyBranch.SI, EarthlyBranch.MAO, EarthlyBranch.CHOU};
            default: throw new IllegalArgumentException("Quái không hợp lệ");
        }
    }

    private static EarthlyBranch[] outerBranches(Trigram trigram) {
        switch (trigram) {
            case QIAN: return new EarthlyBranch[]{EarthlyBranch.WU, EarthlyBranch.SHEN, EarthlyBranch.XU};
            case KUN: return new EarthlyBranch[]{EarthlyBranch.CHOU, EarthlyBranch.HAI, EarthlyBranch.YOU};
            case ZHEN: return new EarthlyBranch[]{EarthlyBranch.WU, EarthlyBranch.SHEN, EarthlyBranch.XU};
            case XUN: return new EarthlyBranch[]{EarthlyBranch.WEI, EarthlyBranch.SI, EarthlyBranch.MAO};
            case KAN: return new EarthlyBranch[]{EarthlyBranch.SHEN, EarthlyBranch.XU, EarthlyBranch.ZI};
            case LI: return new EarthlyBranch[]{EarthlyBranch.YOU, EarthlyBranch.WEI, EarthlyBranch.SI};
            case GEN: return new EarthlyBranch[]{EarthlyBranch.XU, EarthlyBranch.ZI, EarthlyBranch.YIN};
            case DUI: return new EarthlyBranch[]{EarthlyBranch.HAI, EarthlyBranch.YOU, EarthlyBranch.WEI};
            default: throw new IllegalArgumentException("Quái không hợp lệ");
        }
    }

    private static HeavenlyStem innerStem(Trigram trigram) {
        switch (trigram) {
            case QIAN: return HeavenlyStem.JIA;
            case KUN: return HeavenlyStem.YI;
            case ZHEN: return HeavenlyStem.GENG;
            case XUN: return HeavenlyStem.XIN;
            case KAN: return HeavenlyStem.WU;
            case LI: return HeavenlyStem.JI;
            case GEN: return HeavenlyStem.BING;
            case DUI: return HeavenlyStem.DING;
            default: throw new IllegalArgumentException("Quái không hợp lệ");
        }
    }

    private static HeavenlyStem outerStem(Trigram trigram) {
        if (trigram == Trigram.QIAN) return HeavenlyStem.REN;
        if (trigram == Trigram.KUN) return HeavenlyStem.GUI;
        return innerStem(trigram);
    }

    private static SixRelative relativeOf(FiveElement palace, FiveElement line) {
        if (line == palace) return SixRelative.SIBLINGS;
        if (line.generates(palace)) return SixRelative.PARENTS;
        if (palace.generates(line)) return SixRelative.DESCENDANTS;
        if (line.controls(palace)) return SixRelative.OFFICIAL_GHOST;
        if (palace.controls(line)) return SixRelative.WEALTH;
        return SixRelative.SIBLINGS;
    }

    private static int spiritStart(HeavenlyStem stem) {
        switch (stem) {
            case JIA:
            case YI: return 0;
            case BING:
            case DING: return 1;
            case WU: return 2;
            case JI: return 3;
            case GENG:
            case XIN: return 4;
            case REN:
            case GUI: return 5;
            default: return 0;
        }
    }

    private static Set<EarthlyBranch> calculateVoidBranches(HeavenlyStem stem, EarthlyBranch branch) {
        int cycleIndex = -1;
        for (int i = 0; i < 60; i++) {
            if (i % 10 == stem.ordinal() && i % 12 == branch.ordinal()) {
                cycleIndex = i;
                break;
            }
        }
        Set<EarthlyBranch> result = new HashSet<>();
        if (cycleIndex < 0) return result;
        int xunStart = (cycleIndex / 10) * 10;
        boolean[] present = new boolean[12];
        for (int i = 0; i < 10; i++) present[(xunStart + i) % 12] = true;
        for (int i = 0; i < 12; i++) if (!present[i]) result.add(EarthlyBranch.values()[i]);
        return result;
    }

    private static Strength strengthOf(EarthlyBranch line, EarthlyBranch month,
                                       EarthlyBranch day, boolean moving,
                                       boolean isVoid, boolean monthBroken) {
        int score = 0;
        score += relationScore(month.element, line.element, 3, 2, 1, -1, -3);
        score += relationScore(day.element, line.element, 2, 2, 1, -1, -2);
        if (line == month) score += 2;
        if (line == day) score += 1;
        if (line.combines(day)) score += 1;
        if (line.clashes(day)) score -= moving ? 0 : 1;
        if (moving) score += 1;
        if (isVoid) score -= 3;
        if (monthBroken) score -= 3;
        String label;
        if (score >= 6) label = "vượng";
        else if (score >= 3) label = "khá mạnh";
        else if (score >= 0) label = "trung bình";
        else if (score >= -3) label = "yếu";
        else label = "rất yếu";
        return new Strength(score, label + " (" + signed(score) + ")");
    }

    private static int relationScore(FiveElement source, FiveElement target,
                                     int same, int sourceGenerates, int targetGenerates,
                                     int targetControls, int sourceControls) {
        if (source == target) return same;
        if (source.generates(target)) return sourceGenerates;
        if (target.generates(source)) return targetGenerates;
        if (target.controls(source)) return targetControls;
        if (source.controls(target)) return sourceControls;
        return 0;
    }

    private static String buildLineNotes(EarthlyBranch branch, LiuHaoContext context,
                                         boolean moving, boolean isVoid,
                                         boolean monthBroken, EarthlyBranch changed) {
        List<String> notes = new ArrayList<>();
        if (branch == context.monthBranch) notes.add("lâm Nguyệt kiến");
        if (branch == context.dayBranch) notes.add("lâm Nhật thần");
        if (branch.combines(context.dayBranch)) notes.add("hợp Nhật");
        if (branch.clashes(context.dayBranch)) notes.add(moving ? "động gặp Nhật xung" : "tĩnh gặp Nhật xung");
        if (isVoid) notes.add("Không Vong");
        if (monthBroken) notes.add("Nguyệt phá");
        if (moving && changed != null) {
            if (changed == branch) notes.add("hóa đồng chi");
            if (changed.combines(branch)) notes.add("hóa hợp");
            if (changed.clashes(branch)) notes.add("hóa xung");
            if (changed.element.generates(branch.element)) notes.add("hóa hồi đầu sinh");
            if (changed.element.controls(branch.element)) notes.add("hóa hồi đầu khắc");
            if (branch.element.generates(changed.element)) notes.add("động sinh biến");
        }
        return join(notes, ", ");
    }

    private static TargetSelection selectTarget(QuestionTopic topic, NaJiaLine[] lines,
                                                EightPalaceResult palace) {
        if (topic == QuestionTopic.GENERAL || topic == QuestionTopic.HEALTH
                || topic == QuestionTopic.TRAVEL || topic == QuestionTopic.TIMING) {
            NaJiaLine shi = lines[palace.shiLine - 1];
            return new TargetSelection(shi, "Lấy hào Thế làm trọng tâm vì câu hỏi tập trung vào chính người hỏi hoặc diễn biến chung.");
        }
        if (topic == QuestionTopic.LOVE) {
            NaJiaLine ying = lines[palace.yingLine - 1];
            return new TargetSelection(ying, "Lấy hào Ứng làm trọng tâm để quan sát phía đối phương và quan hệ Thế–Ứng.");
        }
        SixRelative desired;
        switch (topic) {
            case FINANCE:
            case LOST_ITEM: desired = SixRelative.WEALTH; break;
            case CAREER:
            case LEGAL: desired = SixRelative.OFFICIAL_GHOST; break;
            case STUDY: desired = SixRelative.PARENTS; break;
            default: desired = SixRelative.SIBLINGS;
        }
        NaJiaLine best = null;
        int bestScore = Integer.MIN_VALUE;
        for (NaJiaLine line : lines) {
            if (line.relative != desired) continue;
            int score = line.strengthScore + (line.moving ? 2 : 0) + (line.shi ? 1 : 0) + (line.ying ? 1 : 0);
            if (score > bestScore) {
                bestScore = score;
                best = line;
            }
        }
        if (best == null) {
            best = lines[palace.shiLine - 1];
            return new TargetSelection(best, "Không thấy " + desired + " lộ rõ trong sáu hào; tạm lấy hào Thế và hạ độ tin cậy.");
        }
        return new TargetSelection(best, "Chủ đề " + topic.label + " ưu tiên " + desired + "; chọn hào " + best.position + " vì biểu hiện rõ nhất trong quẻ.");
    }

    private static String buildJudgment(LiuHaoContext context, EightPalaceResult palace,
                                        NaJiaLine[] lines, NaJiaLine target) {
        NaJiaLine shi = lines[palace.shiLine - 1];
        NaJiaLine ying = lines[palace.yingLine - 1];
        StringBuilder b = new StringBuilder();
        b.append("KẾT LUẬN THEO CHỦ ĐỀ\n");
        b.append("• ").append(topicOpening(context.topic)).append("\n");
        b.append("• Dụng thần tham khảo: ").append(target.relative).append(" tại hào ")
                .append(target.position).append(", ").append(target.strengthLabel).append(".\n");
        int movingCount = 0;
        for (NaJiaLine line : lines) if (line.moving) movingCount++;
        b.append("• Toàn quẻ có ").append(movingCount).append(" hào động; ")
                .append(movingCount == 0 ? "xu hướng thiên về tĩnh và duy trì." : movingCount <= 2 ? "biến động tập trung, dễ nhận diện điểm chuyển." : "nhiều biến số chồng chéo, cần lọc theo Dụng thần.")
                .append("\n");

        if (target.voidBranch) b.append("• Dụng thần rơi Không Vong: việc có thể chưa thành hình, thiếu dữ kiện hoặc phải chờ điều kiện được lấp đầy.\n");
        if (target.monthBroken) b.append("• Dụng thần gặp Nguyệt phá: nền tảng hiện tại yếu, không nên ép tiến độ hoặc đặt cược lớn.\n");
        if (target.moving) b.append("• Dụng thần phát động: sự việc đã có lực chuyển; cần xem hướng biến sang ").append(target.changedBranch).append(".\n");
        if (target.strengthScore >= 4) b.append("• Lực của Dụng thần khá tốt: khả năng tiến triển cao hơn nếu hành động đúng thời điểm.\n");
        else if (target.strengthScore <= -2) b.append("• Lực của Dụng thần yếu: nên ưu tiên phòng thủ, bổ sung nguồn lực và kiểm chứng trước.\n");
        else b.append("• Lực của Dụng thần trung bình: kết quả phụ thuộc nhiều vào cách xử lý và điều kiện bên ngoài.\n");

        if (shi.branch.combines(ying.branch)) b.append("• Thế–Ứng tương hợp: hai phía có điểm gặp nhau, thuận cho thương lượng hoặc phối hợp.\n");
        if (shi.branch.clashes(ying.branch)) b.append("• Thế–Ứng tương xung: kỳ vọng hai phía lệch nhau, dễ phát sinh đổi ý hoặc va chạm.\n");
        if (shi.element.generates(ying.element)) b.append("• Thế sinh Ứng: người hỏi đang bỏ nhiều công sức hơn phía còn lại.\n");
        if (ying.element.generates(shi.element)) b.append("• Ứng sinh Thế: ngoại cảnh hoặc đối phương có xu hướng hỗ trợ người hỏi.\n");
        if (ying.element.controls(shi.element)) b.append("• Ứng khắc Thế: áp lực bên ngoài đang chi phối mạnh; cần giữ giới hạn và phương án dự phòng.\n");

        appendTopicRules(b, context.topic, lines, target, shi, ying);
        b.append("• Đây là mô hình diễn giải quy tắc để tham khảo và nghiệm lý, không phải kết luận chắc chắn.");
        return b.toString();
    }

    private static void appendTopicRules(StringBuilder b, QuestionTopic topic, NaJiaLine[] lines,
                                         NaJiaLine target, NaJiaLine shi, NaJiaLine ying) {
        switch (topic) {
            case FINANCE:
                NaJiaLine siblingMover = firstMoving(lines, SixRelative.SIBLINGS);
                NaJiaLine descendant = strongest(lines, SixRelative.DESCENDANTS);
                if (siblingMover != null) b.append("• Huynh Đệ động: dễ có khoản chia sẻ, cạnh tranh hoặc chi phí làm phân tán dòng tiền.\n");
                if (descendant != null && descendant.strengthScore >= 2) b.append("• Tử Tôn có lực: nguồn tạo doanh thu hoặc đầu ra có tín hiệu hỗ trợ Tài.\n");
                break;
            case CAREER:
                NaJiaLine parents = strongest(lines, SixRelative.PARENTS);
                if (parents != null && parents.strengthScore >= 2) b.append("• Phụ Mẫu có lực: hồ sơ, bằng cấp, quy trình hoặc người nâng đỡ là điểm có lợi.\n");
                if (firstMoving(lines, SixRelative.DESCENDANTS) != null) b.append("• Tử Tôn động khắc Quan: tâm lý muốn thoát áp lực có thể làm giảm tính kỷ luật hoặc thay đổi hướng nghề.\n");
                break;
            case STUDY:
                if (target.strengthScore >= 3) b.append("• Phụ Mẫu vững: khả năng tiếp thu, tài liệu và nền tảng ôn tập đang thuận.\n");
                NaJiaLine official = strongest(lines, SixRelative.OFFICIAL_GHOST);
                if (official != null && official.strengthScore >= 2) b.append("• Quan Quỷ có lực: áp lực thi cử cao nhưng cũng tạo động lực và tính nghiêm túc.\n");
                break;
            case LOVE:
                if (shi.branch.combines(ying.branch)) b.append("• Quan hệ có dấu hiệu muốn kết nối; nên trao đổi rõ nhu cầu và nhịp độ.\n");
                else if (shi.branch.clashes(ying.branch)) b.append("• Hai phía dễ hiểu khác nhau; tránh ép cam kết khi chưa thống nhất vấn đề cốt lõi.\n");
                else b.append("• Quan hệ chưa có tín hiệu hợp/xung quá mạnh; diễn biến phụ thuộc giao tiếp thực tế.\n");
                break;
            case HEALTH:
                NaJiaLine illness = strongest(lines, SixRelative.OFFICIAL_GHOST);
                NaJiaLine relief = strongest(lines, SixRelative.DESCENDANTS);
                if (illness != null && illness.strengthScore >= 3) b.append("• Quan Quỷ có lực: biểu tượng áp lực hoặc triệu chứng đang nổi bật; nên theo dõi và đi khám khi cần.\n");
                if (relief != null && relief.strengthScore >= 2) b.append("• Tử Tôn có lực: biểu tượng phục hồi, nghỉ ngơi hoặc biện pháp hỗ trợ có tác dụng tích cực.\n");
                break;
            case LEGAL:
                if (strongest(lines, SixRelative.PARENTS) != null) b.append("• Phụ Mẫu đại diện giấy tờ/chứng cứ: cần ưu tiên tính đầy đủ, thời hạn và khả năng kiểm chứng.\n");
                if (ying.strengthScore > shi.strengthScore) b.append("• Phía đối ứng đang có thế mạnh hơn; nên chuẩn bị hồ sơ và chiến lược thương lượng kỹ.\n");
                break;
            case TRAVEL:
                if (shi.moving) b.append("• Hào Thế động: có lực di chuyển rõ, nhưng cần xét hướng biến và điều kiện đường đi.\n");
                if (shi.branch.clashes(ying.branch)) b.append("• Thế–Ứng xung: lịch trình có thể đổi, chậm hoặc phát sinh việc ngoài dự kiến.\n");
                break;
            case LOST_ITEM:
                if (target.voidBranch) b.append("• Tài lâm Không: đồ vật có thể bị che khuất, để nhầm nơi hoặc chưa thể tìm ngay.\n");
                if (target.moving) b.append("• Tài động: đồ vật có khả năng đã bị di chuyển khỏi vị trí ban đầu.\n");
                break;
            case TIMING:
                b.append("• Khi hỏi thời điểm, ưu tiên trạng thái động/tĩnh, Không Vong, hợp/xung và địa chi của Dụng thần.\n");
                break;
            default:
                break;
        }
    }

    private static String buildTiming(LiuHaoContext context, NaJiaLine target) {
        StringBuilder b = new StringBuilder("CỬA SỔ THỜI GIAN THAM KHẢO\n");
        if (target.voidBranch) {
            b.append("• Ưu tiên thời điểm địa chi ").append(target.branch)
                    .append(" được lấp hoặc ngày xung ").append(target.branch.opposite())
                    .append(" để trạng thái Không Vong được kích hoạt.");
        } else if (target.monthBroken) {
            b.append("• Tránh thúc ép trong tháng ").append(context.monthBranch)
                    .append("; quan sát khi qua tháng hoặc gặp ngày hợp ").append(target.branch.combinePartner()).append(".");
        } else if (target.moving) {
            b.append("• Dụng thần đang động: mốc gần thường gắn với ngày/giờ ").append(target.branch)
                    .append(" hoặc địa chi biến ").append(target.changedBranch).append(".");
        } else if (target.branch.combines(context.dayBranch)) {
            b.append("• Dụng thần bị hợp với Nhật: có thể chờ ngày xung ").append(target.branch.opposite())
                    .append(" để sự việc thoát trạng thái ràng buộc.");
        } else {
            b.append("• Dụng thần tĩnh: quan sát ngày ").append(target.branch)
                    .append(", ngày hợp ").append(target.branch.combinePartner())
                    .append(" hoặc khi có tác nhân thực tế kích hoạt.");
        }
        b.append("\n• Đơn vị ngày/tuần/tháng phải chọn theo bản chất câu hỏi; app không coi đây là dự báo thời điểm chắc chắn.");
        return b.toString();
    }

    private static String confidence(NaJiaLine[] lines, NaJiaLine target) {
        int moving = 0;
        for (NaJiaLine line : lines) if (line.moving) moving++;
        int score = 2;
        if (target.relative != SixRelative.SIBLINGS) score++;
        if (!target.voidBranch && !target.monthBroken) score++;
        if (Math.abs(target.strengthScore) >= 3) score++;
        if (moving >= 4) score--;
        if (target.voidBranch && target.monthBroken) score--;
        if (score >= 5) return "Độ nhất quán: CAO – Dụng thần rõ và tín hiệu tương đối đồng thuận.";
        if (score >= 3) return "Độ nhất quán: TRUNG BÌNH – có tín hiệu chính nhưng vẫn tồn tại yếu tố trái chiều.";
        return "Độ nhất quán: THẤP – Dụng thần yếu/ẩn hoặc quá nhiều biến động; nên nghiệm lý thận trọng.";
    }

    private static String buildTechnical(LiuHaoContext context, EightPalaceResult palace,
                                         NaJiaLine[] lines, TargetSelection target) {
        StringBuilder b = new StringBuilder();
        b.append("BẢNG NẠP GIÁP – LỤC THÂN – LỤC THẦN\n");
        b.append(palace.summary()).append("\n");
        b.append("Nguyệt: ").append(context.monthBranch).append(" · Nhật: ")
                .append(context.dayStem).append(context.dayBranch).append("\n");
        b.append("Chọn Dụng thần: ").append(target.reason).append("\n\n");
        for (int i = lines.length - 1; i >= 0; i--) {
            b.append(lines[i].compact());
            if (i > 0) b.append("\n");
        }
        return b.toString();
    }

    private static NaJiaLine firstMoving(NaJiaLine[] lines, SixRelative relative) {
        for (NaJiaLine line : lines) if (line.relative == relative && line.moving) return line;
        return null;
    }

    private static NaJiaLine strongest(NaJiaLine[] lines, SixRelative relative) {
        NaJiaLine best = null;
        for (NaJiaLine line : lines) {
            if (line.relative != relative) continue;
            if (best == null || line.strengthScore > best.strengthScore) best = line;
        }
        return best;
    }

    private static String topicOpening(QuestionTopic topic) {
        switch (topic) {
            case FINANCE: return "Xét khả năng tạo và giữ dòng tiền, không thay cho quyết định đầu tư.";
            case CAREER: return "Xét vị thế công việc, áp lực trách nhiệm và điều kiện hồ sơ.";
            case STUDY: return "Xét nền tảng học tập, tài liệu, kỷ luật và áp lực thi cử.";
            case LOVE: return "Xét quan hệ Thế–Ứng và mức độ hỗ trợ/xung đột giữa hai phía.";
            case HEALTH: return "Xét biểu tượng sức chịu đựng và phục hồi; mọi triệu chứng thực tế vẫn cần chuyên môn y tế.";
            case LEGAL: return "Xét giấy tờ, đối ứng và áp lực tranh chấp; không thay tư vấn pháp lý.";
            case TRAVEL: return "Xét lực di chuyển, thay đổi lịch trình và yếu tố bên ngoài.";
            case LOST_ITEM: return "Xét trạng thái của Tài và dấu hiệu dịch chuyển/che khuất.";
            case TIMING: return "Xét trạng thái động, hợp, xung, Không Vong và địa chi kích hoạt.";
            default: return "Xét tổng thể qua hào Thế, hào Ứng, Dụng thần và các hào động.";
        }
    }

    private static String join(List<String> values, String delimiter) {
        StringBuilder b = new StringBuilder();
        for (String value : values) {
            if (b.length() > 0) b.append(delimiter);
            b.append(value);
        }
        return b.toString();
    }

    private static String signed(int value) {
        return value >= 0 ? "+" + value : String.valueOf(value);
    }

    private static final class Strength {
        final int score;
        final String label;
        Strength(int score, String label) { this.score = score; this.label = label; }
    }

    private static final class TargetSelection {
        final NaJiaLine line;
        final String reason;
        TargetSelection(NaJiaLine line, String reason) { this.line = line; this.reason = reason; }
    }
}
