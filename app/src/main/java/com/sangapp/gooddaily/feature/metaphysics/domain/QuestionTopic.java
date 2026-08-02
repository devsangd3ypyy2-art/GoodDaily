package com.sangapp.gooddaily.feature.metaphysics.domain;

public enum QuestionTopic {
    GENERAL("Tổng quát"),
    CAREER("Công việc / sự nghiệp"),
    FINANCE("Tài chính / kinh doanh"),
    LOVE("Tình cảm / quan hệ"),
    HEALTH("Sức khỏe"),
    STUDY("Học tập / thi cử"),
    TRAVEL("Đi lại / xuất hành"),
    LEGAL("Hợp đồng / tranh chấp"),
    LOST_ITEM("Tìm đồ thất lạc"),
    TIMING("Hỏi thời điểm");

    public final String label;

    QuestionTopic(String label) {
        this.label = label;
    }

    @Override public String toString() {
        return label;
    }

    public static QuestionTopic infer(String question) {
        String q = question == null ? "" : question.toLowerCase();
        if (containsAny(q, "tiền", "tài chính", "kinh doanh", "lợi nhuận", "thu nhập", "mua", "bán", "đầu tư")) return FINANCE;
        if (containsAny(q, "yêu", "tình cảm", "người yêu", "vợ", "chồng", "hôn nhân", "quan hệ")) return LOVE;
        if (containsAny(q, "sức khỏe", "bệnh", "đau", "điều trị", "thuốc", "hồi phục")) return HEALTH;
        if (containsAny(q, "thi", "học", "điểm", "toeic", "bằng", "chứng chỉ")) return STUDY;
        if (containsAny(q, "việc làm", "công việc", "sự nghiệp", "thăng chức", "phỏng vấn", "xin việc")) return CAREER;
        if (containsAny(q, "đi", "xuất hành", "du lịch", "chuyến", "đến nơi", "trở về")) return TRAVEL;
        if (containsAny(q, "kiện", "tranh chấp", "hợp đồng", "pháp lý", "giấy tờ")) return LEGAL;
        if (containsAny(q, "mất", "thất lạc", "tìm đồ", "ở đâu")) return LOST_ITEM;
        if (containsAny(q, "khi nào", "bao giờ", "thời điểm", "ngày nào")) return TIMING;
        return GENERAL;
    }

    private static boolean containsAny(String value, String... tokens) {
        for (String token : tokens) if (value.contains(token)) return true;
        return false;
    }
}
