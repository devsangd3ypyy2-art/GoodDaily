package com.sangapp.gooddaily.feature;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FeatureCatalog {
    public static final String FINANCE_RECURRING = "finance_recurring";
    public static final String FINANCE_BUDGET = "finance_category_budget";
    public static final String FINANCE_SAVING = "finance_saving_goal";
    public static final String FINANCE_DEBT = "finance_debt";
    public static final String FINANCE_RECEIPT = "finance_receipt";

    public static final String HEALTH_FOOD = "health_food_preset";
    public static final String HEALTH_WATER = "health_water";
    public static final String HEALTH_SLEEP = "health_sleep";
    public static final String HEALTH_MOOD = "health_mood";
    public static final String HEALTH_WORKOUT = "health_workout";
    public static final String HEALTH_MEDICATION = "health_medication";
    public static final String HEALTH_MEASUREMENT = "health_measurement";

    public static final String PLAN_EVENT = "plan_calendar_event";
    public static final String PLAN_TEMPLATE = "plan_schedule_template";
    public static final String PLAN_TASK = "plan_project_task";
    public static final String LEARNING_SUBJECT = "learning_subject";
    public static final String LEARNING_GOAL = "learning_goal";
    public static final String LEARNING_POMODORO = "learning_pomodoro";
    public static final String HABIT_PLAN = "habit_plan";
    public static final String PERSONAL_GOAL = "personal_goal";
    public static final String JOURNAL_ENTRY = "journal_entry";
    public static final String DIVINATION_ENTRY = "divination_entry";

    public static final String DRIVER_SHIFT = "driver_shift";
    public static final String DRIVER_VEHICLE = "driver_vehicle";
    public static final String DRIVER_FUEL = "driver_fuel";
    public static final String DRIVER_MAINTENANCE = "driver_maintenance";

    public static final String ADVANCED_REMINDER = "advanced_reminder";

    private static final Map<String, FeatureDefinition> DEFINITIONS = new LinkedHashMap<>();

    static {
        add(new FeatureDefinition("FINANCE", FINANCE_RECURRING, "Giao dịch định kỳ",
                "Lưu tiền nhà, Internet, học phí và các khoản lặp lại.",
                "Tên khoản định kỳ", "Nội dung / tài khoản tiền", "Số tiền", "Chu kỳ ngày", "Ngày lặp", "EXPENSE/INCOME · DAILY/WEEKLY/MONTHLY/YEARLY", "₫", true, true));
        add(new FeatureDefinition("FINANCE", FINANCE_BUDGET, "Ngân sách theo danh mục",
                "Đặt hạn mức riêng cho ăn uống, đi lại, học tập và các nhóm khác.",
                "Tên danh mục", "Tháng áp dụng / ghi chú", "Hạn mức", "Đã chi", "Mức cảnh báo %", "Đang dùng / tạm dừng", "₫", false, false));
        add(new FeatureDefinition("FINANCE", FINANCE_SAVING, "Mục tiêu tiết kiệm",
                "Theo dõi quỹ dự phòng hoặc mục tiêu mua sắm bằng thanh tiến độ.",
                "Tên mục tiêu", "Mục đích / hạn hoàn thành", "Số tiền mục tiêu", "Đã tiết kiệm", "Ưu tiên", "Đang thực hiện / hoàn thành", "₫", false, true));
        add(new FeatureDefinition("FINANCE", FINANCE_DEBT, "Quản lý nợ",
                "Ghi tôi nợ ai, ai nợ tôi và lịch sử số tiền đã thanh toán.",
                "Tên người / khoản nợ", "Tôi nợ hoặc người khác nợ tôi", "Tổng nợ", "Còn lại", "Số lần trả", "Đang nợ / đã xong", "₫", true, true));
        add(new FeatureDefinition("FINANCE", FINANCE_RECEIPT, "Hóa đơn và chứng từ",
                "Đính kèm ảnh hóa đơn hoặc tài liệu cho giao dịch.",
                "Tên hóa đơn", "Giao dịch / cửa hàng / ghi chú", "Số tiền", "Thuế / phí", "Số lượng", "Đã đối soát / chưa", "₫", true, true));

        add(new FeatureDefinition("HEALTH", HEALTH_FOOD, "Thực phẩm thường dùng",
                "Tạo món mẫu để nhập nhanh kcal và dinh dưỡng.",
                "Tên thực phẩm", "Khẩu phần / loại bữa", "Kcal", "Protein (g)", "Khối lượng (g)", "Yêu thích / bình thường", "kcal", false, true));
        add(new FeatureDefinition("HEALTH", HEALTH_WATER, "Lượng nước",
                "Theo dõi lượng nước uống và mục tiêu mỗi ngày.",
                "Loại đồ uống", "Ghi chú", "Lượng nước", "Mục tiêu ngày", "Số lần", "Đã hoàn thành / chưa", "ml", true, false));
        add(new FeatureDefinition("HEALTH", HEALTH_SLEEP, "Giấc ngủ",
                "Ghi giờ ngủ, giờ thức, chất lượng và thời lượng.",
                "Giấc ngủ", "Chất lượng / ghi chú", "Số giờ ngủ", "Điểm chất lượng", "Số lần thức", "Tốt / bình thường / kém", "giờ", true, false));
        add(new FeatureDefinition("HEALTH", HEALTH_MOOD, "Tâm trạng",
                "Theo dõi cảm xúc và mức căng thẳng theo ngày.",
                "Cảm xúc", "Nguyên nhân / sự kiện", "Điểm tâm trạng", "Mức căng thẳng", "Năng lượng", "Vui / bình thường / buồn / căng thẳng", "điểm", true, true));
        add(new FeatureDefinition("HEALTH", HEALTH_WORKOUT, "Bài tập",
                "Lưu bài tập, số hiệp, số lần và thời gian vận động.",
                "Tên bài tập", "Nhóm cơ / ghi chú", "Thời gian", "Kcal tiêu hao", "Số hiệp hoặc lần", "Hoàn thành / bỏ dở", "phút", true, true));
        add(new FeatureDefinition("HEALTH", HEALTH_MEDICATION, "Thuốc và lịch uống",
                "Ghi thuốc, liều lượng, giờ uống và trạng thái hoàn thành.",
                "Tên thuốc", "Liều lượng / hướng dẫn", "Số viên", "Khoảng cách giờ", "Số lần mỗi ngày", "Đã uống / chưa uống", "viên", true, true));
        add(new FeatureDefinition("HEALTH", HEALTH_MEASUREMENT, "Số đo cơ thể",
                "Theo dõi eo, ngực, tay, chân và các chỉ số bổ sung.",
                "Loại số đo", "Vị trí / ghi chú", "Kết quả", "Mục tiêu", "Lần đo", "Tăng / giảm / ổn định", "cm", false, true));

        add(new FeatureDefinition("PLANNER", PLAN_EVENT, "Sự kiện lịch",
                "Lịch ngày, tuần, tháng với deadline, ưu tiên và nhắc nhở.",
                "Tên sự kiện", "Địa điểm / mô tả", "Thời lượng", "Nhắc trước phút", "Độ ưu tiên", "Một lần / hằng tuần / hằng tháng", "phút", true, true));
        add(new FeatureDefinition("PLANNER", PLAN_TEMPLATE, "Mẫu thời gian biểu",
                "Lưu mẫu học, làm việc, ngủ hoặc tập luyện để sao chép nhanh.",
                "Tên mẫu", "Các ngày áp dụng / mô tả", "Thời lượng", "Nhắc trước", "Số ngày tuần", "Đang dùng / tạm dừng", "phút", true, false));
        add(new FeatureDefinition("PLANNER", PLAN_TASK, "Dự án và công việc",
                "Quản lý deadline, công việc con, mức ưu tiên và trạng thái quá hạn.",
                "Tên công việc", "Dự án / mô tả / công việc con", "Tiến độ %", "Thời gian dự kiến", "Độ ưu tiên", "Chưa làm / đang làm / hoàn thành", "%", true, true));
        add(new FeatureDefinition("LEARNING", LEARNING_SUBJECT, "Danh sách môn học",
                "Tổ chức môn học, mục tiêu và tài liệu.",
                "Tên môn học", "Nội dung / tài liệu", "Mục tiêu giờ tuần", "Đã học", "Độ ưu tiên", "Đang học / tạm dừng", "giờ", false, true));
        add(new FeatureDefinition("LEARNING", LEARNING_GOAL, "Mục tiêu học tập",
                "Theo dõi TOEIC, điểm thi thử, số giờ và tiến độ.",
                "Tên mục tiêu", "Môn / thời hạn", "Điểm hoặc số giờ mục tiêu", "Hiện tại", "Số mốc", "Đang thực hiện / đạt", "điểm", false, true));
        add(new FeatureDefinition("LEARNING", LEARNING_POMODORO, "Lịch sử Pomodoro",
                "Lưu phiên 25, 45, 60 phút và trạng thái tập trung.",
                "Nội dung phiên học", "Môn học / ghi chú", "Thời lượng", "Thời gian nghỉ", "Số vòng", "Hoàn thành / gián đoạn", "phút", true, false));
        add(new FeatureDefinition("HABIT", HABIT_PLAN, "Thói quen nâng cao",
                "Mục tiêu số lượng, ngày thực hiện, nhắc riêng và chuỗi dài nhất.",
                "Tên thói quen", "Các thứ thực hiện / nhắc nhở", "Mục tiêu mỗi ngày", "Đã hoàn thành", "Chuỗi dài nhất", "Đang dùng / tạm dừng", "lần", true, false));
        add(new FeatureDefinition("GOAL", PERSONAL_GOAL, "Mục tiêu cá nhân",
                "Tăng cân, tiết kiệm, học từ mới hoặc tập luyện với tiến độ tự theo dõi.",
                "Tên mục tiêu", "Nguồn dữ liệu / thời hạn", "Mục tiêu", "Hiện tại", "Độ ưu tiên", "Đang thực hiện / hoàn thành", "", false, true));
        add(new FeatureDefinition("JOURNAL", JOURNAL_ENTRY, "Nhật ký nhiều mục",
                "Viết nhiều ghi chú mỗi ngày, gắn tag, ảnh, tìm kiếm và đánh dấu quan trọng.",
                "Tiêu đề nhật ký", "Nội dung", "Điểm cảm xúc", "Giá trị liên quan", "Mức quan trọng", "Riêng tư / bình thường", "", true, true));
        add(new FeatureDefinition("LEGACY", DIVINATION_ENTRY, "Nghiệm lý và gieo quẻ",
                "Lưu Lục Hào, Mai Hoa, kết quả dự đoán và sự việc thực tế.",
                "Câu hỏi / tên quẻ", "Luận giải và kết quả thực tế", "Độ chính xác", "Mốc kiểm chứng", "Số hào động", "Chờ kiểm chứng / đã nghiệm", "%", true, true));

        add(new FeatureDefinition("LEGACY", DRIVER_SHIFT, "Ca chạy và thu nhập",
                "Tính doanh thu, số cuốc, số km, chi phí và lợi nhuận ròng.",
                "Tên ca", "Khu vực / ghi chú", "Doanh thu", "Tổng chi phí", "Số cuốc", "Đã chốt / đang chạy", "₫", true, true));
        add(new FeatureDefinition("LEGACY", DRIVER_VEHICLE, "Phương tiện",
                "Quản lý xe, biển số, số km và thông tin sử dụng.",
                "Tên xe", "Biển số / loại xe", "Số km hiện tại", "Giá mua", "Năm sử dụng", "Đang dùng / ngừng dùng", "km", false, true));
        add(new FeatureDefinition("LEGACY", DRIVER_FUEL, "Nhật ký nhiên liệu",
                "Ghi tiền xăng, số lít, số km và mức tiêu hao.",
                "Lần đổ nhiên liệu", "Cây xăng / ghi chú", "Số tiền", "Số lít", "Số km", "Đầy bình / bổ sung", "₫", true, true));
        add(new FeatureDefinition("LEGACY", DRIVER_MAINTENANCE, "Bảo dưỡng xe",
                "Theo dõi thay nhớt, lốp, phanh, ắc quy và hạn tiếp theo.",
                "Hạng mục bảo dưỡng", "Chi tiết / nơi thực hiện", "Chi phí", "Km bảo dưỡng tiếp", "Số tháng tiếp theo", "Sắp đến hạn / hoàn thành", "₫", true, true));

        add(new FeatureDefinition("REMINDER", ADVANCED_REMINDER, "Nhắc nhở nâng cao",
                "Lặp nhiều ngày, hằng tháng/năm, báo trước, rung và âm thanh riêng.",
                "Tên nhắc nhở", "Nội dung / ngày trong tuần", "Báo trước phút", "Khoảng lặp ngày", "Mức ưu tiên", "Một lần / DAILY / WEEKLY / MONTHLY / YEARLY", "phút", true, true));
    }

    private FeatureCatalog() {}
    private static void add(FeatureDefinition definition) { DEFINITIONS.put(definition.feature, definition); }
    public static FeatureDefinition get(String feature) { return DEFINITIONS.get(feature); }
    public static List<FeatureDefinition> all() { return new ArrayList<>(DEFINITIONS.values()); }
    public static List<FeatureDefinition> byModule(String module) {
        List<FeatureDefinition> result = new ArrayList<>();
        for (FeatureDefinition d : DEFINITIONS.values()) if (module.equals(d.module)) result.add(d);
        return result;
    }
}
