package com.sangapp.gooddaily.feature.metaphysics.domain;

import java.util.Calendar;

public final class AdvancedMaiHoaInterpreter {
    private AdvancedMaiHoaInterpreter() {}

    public static MaiHoaAnalysis analyze(HexagramInfo base, HexagramInfo nuclear,
                                         HexagramInfo changed, Trigram body,
                                         Trigram use, int movingLine,
                                         QuestionTopic topic, String question,
                                         Calendar castTime) {
        QuestionTopic resolvedTopic = topic == null || topic == QuestionTopic.GENERAL
                ? QuestionTopic.infer(question) : topic;
        FiveElement seasonal = seasonElement(castTime == null ? Calendar.getInstance() : castTime);
        int bodyScore = elementSeasonScore(seasonal, body.element);
        int useScore = elementSeasonScore(seasonal, use.element);
        Trigram changedUse = movingLine <= 3 ? changed.lower : changed.upper;

        StringBuilder judgment = new StringBuilder("KẾT LUẬN THEO CHỦ ĐỀ\n");
        judgment.append("• Chủ đề: ").append(resolvedTopic.label).append(".\n");
        judgment.append("• ").append(InterpretationEngine.relationText(body.element, use.element)).append("\n");
        judgment.append("• Thể ").append(body.vietnamese).append(" có khí mùa ")
                .append(strengthText(bodyScore)).append("; Dụng ").append(use.vietnamese)
                .append(" có khí mùa ").append(strengthText(useScore)).append(".\n");
        judgment.append("• Sau biến động, phần Dụng chuyển sang ").append(changedUse.label()).append(": ")
                .append(InterpretationEngine.relationText(body.element, changedUse.element)).append("\n");
        appendTopicJudgment(judgment, resolvedTopic, body, use, changedUse, movingLine,
                bodyScore, useScore);
        judgment.append("• ").append(linePositionMeaning(movingLine)).append("\n");
        judgment.append("• Kết quả dùng để tự suy ngẫm và nghiệm lý, không phải dự báo chắc chắn.");

        StringBuilder technical = new StringBuilder("PHÂN TÍCH THỂ – DỤNG – HỖ – BIẾN\n");
        technical.append("Quẻ chủ: ").append(base.title()).append(" · ").append(base.symbols()).append("\n");
        technical.append("Quẻ hỗ: ").append(nuclear.title()).append(" · nội trình ")
                .append(nuclear.lower.label()).append(" / ").append(nuclear.upper.label()).append("\n");
        technical.append("Quẻ biến: ").append(changed.title()).append(" · ").append(changed.symbols()).append("\n");
        technical.append("Hào động: ").append(movingLine).append(" · ").append(linePositionMeaning(movingLine)).append("\n");
        technical.append("Thể: ").append(body.label()).append(" · điểm khí mùa ").append(signed(bodyScore)).append("\n");
        technical.append("Dụng ban đầu: ").append(use.label()).append(" · điểm khí mùa ").append(signed(useScore)).append("\n");
        technical.append("Dụng sau biến: ").append(changedUse.label()).append("\n");
        technical.append("Tượng quẻ theo chủ đề:\n")
                .append("• Thượng quái ").append(base.upper.vietnamese).append(": ").append(imageMeaning(base.upper, resolvedTopic)).append("\n")
                .append("• Hạ quái ").append(base.lower.vietnamese).append(": ").append(imageMeaning(base.lower, resolvedTopic)).append("\n")
                .append("• Quẻ hỗ nhấn mạnh: ").append(imageMeaning(nuclear.lower, resolvedTopic))
                .append("; ").append(imageMeaning(nuclear.upper, resolvedTopic)).append(".");

        String timing = buildTiming(movingLine, resolvedTopic, bodyScore, useScore, changedUse, body);
        String confidence = buildConfidence(bodyScore, useScore, body, use, changedUse, movingLine);
        return new MaiHoaAnalysis(judgment.toString(), technical.toString(), timing, confidence);
    }

    private static void appendTopicJudgment(StringBuilder b, QuestionTopic topic,
                                            Trigram body, Trigram use, Trigram changedUse,
                                            int movingLine, int bodyScore, int useScore) {
        switch (topic) {
            case FINANCE:
                if (body.element.controls(use.element)) b.append("• Tài chính: Thể khắc Dụng cho thấy có khả năng kiểm soát nguồn lực, nhưng phải chủ động quản trị chi phí.\n");
                else if (use.element.controls(body.element)) b.append("• Tài chính: Dụng khắc Thể cho thấy áp lực chi phí, giá cả hoặc nghĩa vụ đang lấn át khả năng giữ tiền.\n");
                else if (use.element.generates(body.element)) b.append("• Tài chính: Dụng sinh Thể, có dấu hiệu nguồn lực hoặc cơ hội từ bên ngoài hỗ trợ.\n");
                if (changedUse.element.controls(body.element)) b.append("• Xu hướng biến làm áp lực tăng; tránh quyết định lớn khi chưa có quỹ dự phòng.\n");
                break;
            case CAREER:
                b.append("• Công việc: ").append(careerImage(use)).append("\n");
                if (movingLine >= 4) b.append("• Biến động ở ngoại quái: thay đổi dễ đến từ tổ chức, cấp trên, khách hàng hoặc môi trường làm việc.\n");
                else b.append("• Biến động ở nội quái: thay đổi bắt đầu từ kỹ năng, thái độ hoặc lựa chọn cá nhân.\n");
                break;
            case STUDY:
                b.append("• Học tập: ").append(studyImage(baseQuality(body, use))).append("\n");
                if (bodyScore < 0) b.append("• Thể yếu theo mùa: cần giảm mục tiêu ngắn hạn, chia nhỏ phiên học và ưu tiên nền tảng.\n");
                break;
            case LOVE:
                if (body.element == use.element) b.append("• Tình cảm: hai phía có điểm đồng điệu nhưng dễ đứng yên nếu không chủ động nói rõ.\n");
                else if (use.element.generates(body.element)) b.append("• Tình cảm: phía đối ứng có xu hướng nuôi dưỡng hoặc nhường nhịn người hỏi.\n");
                else if (body.element.generates(use.element)) b.append("• Tình cảm: người hỏi đang cho đi nhiều hơn, cần xem sự đáp lại có cân bằng không.\n");
                else if (use.element.controls(body.element)) b.append("• Tình cảm: cảm giác bị áp lực hoặc bị dẫn dắt khá rõ; nên giữ ranh giới.\n");
                break;
            case HEALTH:
                b.append("• Sức khỏe: Thể tượng cho sức chịu đựng của bản thân, Dụng tượng cho tác nhân hoặc thói quen ảnh hưởng.\n");
                if (bodyScore < useScore) b.append("• Thể yếu hơn Dụng: ưu tiên nghỉ ngơi, theo dõi triệu chứng và tìm hỗ trợ chuyên môn khi cần.\n");
                else b.append("• Thể có lực hơn Dụng: biểu tượng phục hồi tương đối tốt, nhưng vẫn cần dựa trên dữ liệu sức khỏe thực tế.\n");
                break;
            case TRAVEL:
                b.append("• Xuất hành: ").append(travelImage(use)).append("\n");
                if (movingLine >= 4) b.append("• Ngoại quái động: dễ có đổi lịch, giao thông hoặc yếu tố bên ngoài tác động.\n");
                break;
            case LEGAL:
                b.append("• Giấy tờ/tranh chấp: ưu tiên bằng chứng, thời hạn và cách diễn đạt; tượng ")
                        .append(use.vietnamese).append(" cho thấy ").append(legalImage(use)).append("\n");
                break;
            case LOST_ITEM:
                b.append("• Tìm đồ: tượng Dụng ").append(use.vietnamese).append(" gợi hướng ")
                        .append(directionHint(use)).append(" và môi trường ").append(use.image.toLowerCase()).append(".\n");
                break;
            case TIMING:
                b.append("• Hỏi thời điểm: lấy hào động làm chỉ dấu nhịp, quẻ biến làm trạng thái sau khi điều kiện chín.\n");
                break;
            default:
                b.append("• Tổng quát: so sánh Thể với Dụng ban đầu và Dụng sau biến để nhìn lực của bản thân, hoàn cảnh và xu hướng.\n");
                break;
        }
        if (movingLine == 3 || movingLine == 6) b.append("• Hào ở ranh giới/cực điểm: tránh hành động quá tay, nên có bước kiểm tra trước khi chuyển pha.\n");
    }

    private static String buildTiming(int movingLine, QuestionTopic topic,
                                      int bodyScore, int useScore,
                                      Trigram changedUse, Trigram body) {
        String unit;
        switch (topic) {
            case TRAVEL:
            case LOST_ITEM: unit = "ngày"; break;
            case CAREER:
            case STUDY:
            case FINANCE: unit = "tuần"; break;
            case LOVE:
            case HEALTH: unit = "tuần đến tháng"; break;
            default: unit = "đơn vị thời gian phù hợp với câu hỏi"; break;
        }
        StringBuilder b = new StringBuilder("CỬA SỔ THỜI GIAN THAM KHẢO\n");
        b.append("• Hào ").append(movingLine).append(" gợi nhịp khoảng ").append(movingLine)
                .append(" ").append(unit).append(", nhưng phải đối chiếu tiến độ thực tế.\n");
        if (changedUse.element.generates(body.element)) b.append("• Quẻ biến sinh Thể: kết quả dễ rõ hơn sau khi bước chuyển hoàn tất.\n");
        else if (changedUse.element.controls(body.element)) b.append("• Quẻ biến khắc Thể: nên lùi thời điểm hoặc chờ điều kiện giảm áp lực.\n");
        if (bodyScore >= useScore) b.append("• Thể không yếu hơn Dụng: có thể chủ động tạo mốc hành động thay vì chỉ chờ đợi.");
        else b.append("• Dụng mạnh hơn Thể: nên chờ thêm dữ kiện hoặc nguồn lực trước khi chốt thời điểm.");
        return b.toString();
    }

    private static String buildConfidence(int bodyScore, int useScore, Trigram body,
                                          Trigram use, Trigram changedUse, int movingLine) {
        int score = 2;
        if (Math.abs(bodyScore - useScore) >= 2) score++;
        if (use.element == changedUse.element || use.element.generates(changedUse.element)) score++;
        if (movingLine == 3 || movingLine == 6) score--;
        if (body.element == use.element) score++;
        if (score >= 4) return "Độ nhất quán: CAO – quan hệ Thể–Dụng và xu hướng biến tương đối rõ.";
        if (score >= 2) return "Độ nhất quán: TRUNG BÌNH – có trục chính nhưng vẫn cần hoàn cảnh thực tế để phân biệt.";
        return "Độ nhất quán: THẤP – tín hiệu chuyển pha hoặc xung đột nhiều; nên nghiệm lý thận trọng.";
    }

    private static FiveElement seasonElement(Calendar calendar) {
        int month = calendar.get(Calendar.MONTH) + 1;
        if (month >= 3 && month <= 5) return FiveElement.WOOD;
        if (month >= 6 && month <= 8) return FiveElement.FIRE;
        if (month >= 9 && month <= 11) return FiveElement.METAL;
        return FiveElement.WATER;
    }

    private static int elementSeasonScore(FiveElement season, FiveElement target) {
        if (season == target) return 3;
        if (season.generates(target)) return 2;
        if (target.generates(season)) return 1;
        if (target.controls(season)) return -1;
        if (season.controls(target)) return -3;
        return 0;
    }

    private static String strengthText(int score) {
        if (score >= 3) return "vượng";
        if (score >= 1) return "được trợ";
        if (score == 0) return "trung bình";
        if (score >= -1) return "hao";
        return "suy";
    }

    private static String linePositionMeaning(int line) {
        switch (line) {
            case 1: return "Hào 1: giai đoạn khởi đầu, nền móng và việc cần làm ngay từ gốc";
            case 2: return "Hào 2: phối hợp gần, nguồn lực thực tế và cách làm ổn định";
            case 3: return "Hào 3: ranh giới chuyển pha, dễ vội hoặc mắc lỗi do quá sức";
            case 4: return "Hào 4: bước vào môi trường bên ngoài, gặp người hoặc cơ hội mới";
            case 5: return "Hào 5: trung tâm quyết định, vai trò lãnh đạo hoặc điều kiện then chốt";
            case 6: return "Hào 6: cực điểm, kết thúc chu kỳ; cần tránh quá đà và chuẩn bị đổi pha";
            default: return "Vị trí hào chưa xác định";
        }
    }

    private static String imageMeaning(Trigram trigram, QuestionTopic topic) {
        switch (trigram) {
            case QIAN: return "chủ động, quy tắc, người có quyền quyết định";
            case KUN: return "tiếp nhận, nền tảng, kiên trì và việc cần nuôi dưỡng";
            case ZHEN: return "khởi động nhanh, tin tức bất ngờ, hành động đầu tiên";
            case XUN: return "thẩm thấu, thương lượng, tiến từng bước và tác động mềm";
            case KAN: return "rủi ro, khoảng trống thông tin, dòng chảy và việc phải thận trọng";
            case LI: return "sự rõ ràng, hình ảnh, giấy tờ, kết quả cần được nhìn thấy";
            case GEN: return "dừng, giới hạn, tích lũy và thời điểm nên giữ nguyên";
            case DUI: return "giao tiếp, niềm vui, trao đổi, lời hứa và sự hấp dẫn";
            default: return "tượng chưa xác định";
        }
    }

    private static String careerImage(Trigram trigram) {
        switch (trigram) {
            case QIAN: return "cơ hội gắn với trách nhiệm hoặc người có thẩm quyền";
            case KUN: return "cần làm chắc nền tảng và chấp nhận tiến chậm";
            case ZHEN: return "có tín hiệu khởi động hoặc thay đổi đột ngột";
            case XUN: return "đàm phán, quan hệ và kỹ năng mềm quyết định nhiều";
            case KAN: return "cần quản trị rủi ro và tránh thông tin mơ hồ";
            case LI: return "hồ sơ, hình ảnh cá nhân và khả năng trình bày là điểm then chốt";
            case GEN: return "có giai đoạn chững; nên củng cố kỹ năng trước khi tiến";
            case DUI: return "giao tiếp, phỏng vấn hoặc khách hàng có vai trò lớn";
            default: return "cần quan sát thêm";
        }
    }

    private static String studyImage(int quality) {
        if (quality >= 2) return "nền tảng và hoàn cảnh hỗ trợ nhau; phù hợp tăng cường độ có kiểm soát";
        if (quality <= -2) return "dễ phân tâm hoặc quá tải; nên quay về kế hoạch nhỏ và đều";
        return "tiến độ ở mức trung bình; kết quả phụ thuộc tính đều đặn hơn là học dồn";
    }

    private static int baseQuality(Trigram body, Trigram use) {
        if (use.element.generates(body.element)) return 3;
        if (body.element == use.element) return 2;
        if (body.element.controls(use.element)) return 1;
        if (body.element.generates(use.element)) return -1;
        if (use.element.controls(body.element)) return -3;
        return 0;
    }

    private static String travelImage(Trigram trigram) {
        switch (trigram) {
            case ZHEN: return "khởi hành nhanh nhưng dễ có thay đổi bất ngờ";
            case XUN: return "đi theo lộ trình mềm, có điều chỉnh từng bước";
            case KAN: return "cần đề phòng đường khó, chậm trễ hoặc thông tin thiếu";
            case GEN: return "có dấu hiệu dừng/chờ, nên kiểm tra điều kiện trước khi đi";
            case LI: return "giấy tờ, bản đồ, vé hoặc thông tin hiển thị cần kiểm tra kỹ";
            default: return "lịch trình nên được đối chiếu với điều kiện thực tế";
        }
    }

    private static String legalImage(Trigram trigram) {
        switch (trigram) {
            case LI: return "chứng cứ và nội dung văn bản phải minh bạch";
            case DUI: return "lời nói, thỏa thuận và cách thương lượng rất quan trọng";
            case KAN: return "còn rủi ro hoặc chi tiết chưa được làm rõ";
            case GEN: return "có khả năng đình lại hoặc cần chờ thủ tục";
            case QIAN: return "quy tắc và thẩm quyền là yếu tố quyết định";
            default: return "cần giữ hồ sơ đầy đủ và tránh diễn giải cảm tính";
        }
    }

    private static String directionHint(Trigram trigram) {
        switch (trigram) {
            case QIAN: return "Tây Bắc / nơi cao, gần đồ kim loại";
            case KUN: return "Tây Nam / nơi thấp, kho hoặc nền đất";
            case ZHEN: return "Đông / gần đồ chuyển động hoặc nơi có tiếng động";
            case XUN: return "Đông Nam / khe, góc, túi hoặc nơi có gió";
            case KAN: return "Bắc / gần nước, chỗ tối hoặc hốc sâu";
            case LI: return "Nam / gần điện, ánh sáng, màn hình hoặc giấy tờ";
            case GEN: return "Đông Bắc / cạnh tường, ngăn tủ hoặc nơi bị chặn";
            case DUI: return "Tây / gần cửa miệng, đồ uống hoặc nơi giao tiếp";
            default: return "chưa xác định";
        }
    }

    private static String signed(int value) {
        return value >= 0 ? "+" + value : String.valueOf(value);
    }
}
