package com.sangapp.gooddaily.feature.metaphysics.domain;

public final class InterpretationEngine {
    private InterpretationEngine() {}

    public static String relationText(FiveElement body, FiveElement use) {
        if (body == use) return "Thể và Dụng đồng hành " + body.vietnamese + ": tình thế tương đối cân bằng, nên giữ nhịp ổn định.";
        if (use.generates(body)) return "Dụng " + use.vietnamese + " sinh Thể " + body.vietnamese + ": có xu hướng nhận được hỗ trợ hoặc điều kiện thuận lợi.";
        if (body.generates(use)) return "Thể " + body.vietnamese + " sinh Dụng " + use.vietnamese + ": dễ phải bỏ công, tiền hoặc năng lượng trước khi thấy kết quả.";
        if (use.controls(body)) return "Dụng " + use.vietnamese + " khắc Thể " + body.vietnamese + ": có áp lực từ hoàn cảnh, cần thận trọng và giảm đối đầu.";
        if (body.controls(use)) return "Thể " + body.vietnamese + " khắc Dụng " + use.vietnamese + ": có khả năng kiểm soát tình thế nhưng sẽ cần kỷ luật và chủ động.";
        return "Quan hệ ngũ hành ở trạng thái trung tính, nên xét thêm hoàn cảnh thực tế.";
    }

    public static String buildMaiHoa(HexagramInfo base, HexagramInfo nuclear, HexagramInfo changed,
                                     Trigram body, Trigram use, int movingLine) {
        String relation = relationText(body.element, use.element);
        String motion = movingLine <= 3
                ? "Biến động nằm ở nội quái, thường phản ánh nguyên nhân bên trong, nền tảng hoặc quyết định của chính mình."
                : "Biến động nằm ở ngoại quái, thường phản ánh môi trường, người khác hoặc yếu tố bên ngoài.";
        return "Quẻ chủ " + base.title() + " cho thấy hình thế ban đầu là " + base.upper.image + " ở trên " + base.lower.image + ". "
                + "Quẻ hỗ " + nuclear.title() + " dùng để quan sát quá trình bên trong; quẻ biến " + changed.title() + " mô tả xu hướng sau biến động. "
                + relation + " " + motion + " Hào " + movingLine + " là điểm cần chú ý nhất. "
                + "Nên dùng kết quả như một khung tự suy ngẫm, đồng thời đối chiếu dữ kiện thực tế trước khi quyết định.";
    }

    public static String buildLiuHao(HexagramInfo base, HexagramInfo changed, HexagramInfo nuclear, int[] movingLines) {
        String movement;
        if (movingLines.length == 0) {
            movement = "Không có hào động: tình thế thiên về ổn định; trọng tâm là hiểu đúng quẻ chủ và duy trì nguyên tắc.";
        } else if (movingLines.length == 1) {
            movement = "Có một hào động: tập trung vào hào " + movingLines[0] + " vì đây là điểm chuyển chính.";
        } else if (movingLines.length <= 3) {
            movement = "Có " + movingLines.length + " hào động: tình thế đang chuyển rõ, nên xem đồng thời quẻ chủ và quẻ biến, tránh kết luận từ một dấu hiệu duy nhất.";
        } else {
            movement = "Có nhiều hào động: biến động mạnh và nhiều yếu tố chồng chéo; nên ưu tiên xu hướng của quẻ biến và kiểm chứng bằng thực tế.";
        }
        String relation = relationText(base.lower.element, base.upper.element);
        return "Quẻ chủ " + base.title() + " gồm " + base.upper.image + " ở trên " + base.lower.image + ". "
                + "Quẻ hỗ " + nuclear.title() + " phản ánh cấu trúc bên trong; quẻ biến " + changed.title() + " cho thấy hướng chuyển. "
                + relation + " " + movement + " Đây là phần luận giải biểu tượng nền tảng, chưa thay thế phép nạp giáp và định dụng thần chuyên sâu.";
    }
}
