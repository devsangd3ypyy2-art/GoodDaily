# Good Daily v2.4.0

Good Daily là ứng dụng quản lý đời sống cá nhân chạy offline bằng Android Java, XML, MVVM và Room.

## Mục tiêu của v2.4.0

Bản này xử lý vấn đề các tính năng nâng cao bị giấu trong “Trung tâm quản lý”. Các công cụ đã được đưa về đúng màn hình sở hữu chúng:

- **Tài chính**: ví local, tìm giao dịch, phân tích, giao dịch định kỳ, ngân sách danh mục, tiết kiệm, nợ, hóa đơn, ca chạy và phương tiện.
- **Sức khỏe**: BMR/TDEE, báo cáo, thực phẩm mẫu, nước, ngủ, tâm trạng, bài tập, thuốc và số đo.
- **Kế hoạch**: lịch tháng, sự kiện, mẫu thời gian biểu, dự án, nhắc nhở, môn học, mục tiêu học tập, Pomodoro, thói quen, mục tiêu cá nhân, nhật ký và Dịch học.
- **Cá nhân**: thông báo, âm thanh, bảo mật, backup, tìm kiếm và danh mục tất cả chức năng.
- **Tổng quan**: lối tắt trực tiếp tới ca chạy, lịch tháng, báo cáo sức khỏe và Dịch học.

“Trung tâm quản lý” được đổi thành **Tất cả chức năng** và chỉ còn là danh mục dự phòng.

## Cải tiến kiến trúc

- Thêm `ui/common/ModuleToolsRenderer.java` để tạo card công cụ thống nhất.
- Thêm `ui/common/FeatureNavigator.java` để điều hướng an toàn tới màn hình chuyên biệt hoặc `FeatureManagerFragment`.
- Thêm `FeaturePlacementCatalog.java` và unit test để bảo đảm mọi chức năng trong `FeatureCatalog` đều có vị trí hiển thị rõ ràng.
- Không thay đổi schema Room nên dữ liệu cũ được giữ nguyên.

## Thông tin project

- Package: `com.sangapp.gooddaily`
- Database: `good_daily_database`
- Room version: `6`
- Version name: `2.4.0`
- Version code: `26`
- Min SDK: `25`

## Chạy project

1. Mở thư mục chứa trực tiếp `app`, `gradle`, `build.gradle`, `settings.gradle`.
2. Chờ Gradle Sync.
3. Chọn **Build → Clean Project**.
4. Chọn **Build → Rebuild Project**.
5. Run trên máy ảo hoặc thiết bị.

## Lưu ý

Mai Hoa/Lục Hào là công cụ ghi chép và diễn giải tham khảo cá nhân, không nên dùng làm căn cứ duy nhất cho quyết định sức khỏe, pháp lý, tài chính hoặc an toàn.
