# Feature placement matrix v2.4.0

| Màn hình chính | Chức năng hiển thị trực tiếp |
|---|---|
| Tổng quan | Ca chạy, Lịch tháng, Báo cáo sức khỏe, Dịch học, Tìm kiếm toàn app |
| Tài chính | Ví local, lọc giao dịch, phân tích, định kỳ, ngân sách, tiết kiệm, nợ, hóa đơn, ca chạy & phương tiện |
| Sức khỏe | BMR/TDEE, báo cáo, thực phẩm mẫu, nước, ngủ, tâm trạng, bài tập, thuốc, số đo |
| Kế hoạch | Lịch tháng, sự kiện, mẫu lịch, dự án, nhắc nhở, môn học, mục tiêu học, Pomodoro, thói quen, mục tiêu cá nhân, nhật ký, Mai Hoa/Lục Hào |
| Cá nhân | Thông báo, âm thanh & nhạc, bảo mật, backup, tìm kiếm, tất cả chức năng |

## Nguyên tắc

- Chức năng chuyên biệt mở màn hình chuyên biệt.
- Chức năng dữ liệu linh hoạt mở `FeatureManagerFragment` với đúng `featureKey`.
- “Tất cả chức năng” chỉ là danh mục dự phòng, không phải đường vào bắt buộc.
