# Hướng dẫn cập nhật Good Daily lên 1.3.0

## Cập nhật giữ nguyên dữ liệu

1. Mở bản Good Daily hiện tại.
2. Vào **Cá nhân > Xuất dữ liệu** để tạo một file JSON dự phòng.
3. Không gỡ ứng dụng khỏi máy ảo hoặc điện thoại.
4. Không chọn **Clear Data**.
5. Giải nén `GoodDaily_v1.3.0_full.zip`.
6. Mở thư mục project có trực tiếp các mục `app`, `gradle`, `build.gradle`, `settings.gradle`.
7. Sync Gradle rồi Run trên đúng thiết bị đang cài bản cũ.

Package vẫn là `com.sangapp.gooddaily` và database vẫn là `good_daily_database`. Room sẽ chạy Migration `2 -> 3`, tạo các bảng mới nhưng giữ giao dịch, sức khỏe, công việc, thói quen, ghi chú và nhắc nhở cũ.

## Sau khi cập nhật

- Vào **Tài chính > Số tiền hiện có** để nhập tiền mặt, ngân hàng và ví điện tử lúc bắt đầu theo dõi.
- Vào **Kế hoạch > Thời gian biểu** để thêm giờ học, giờ làm, giờ ngủ hoặc nghỉ.
- Trong **Trạm học tập**, nhập số lượng từ mới và điểm thi thử của ngày đang chọn.
- Vào **Cá nhân**, chạm ảnh đại diện để chọn ảnh từ máy.

## Khi muốn chạy như app mới hoàn toàn

Dùng máy ảo khác hoặc gỡ ứng dụng. Việc gỡ app/Clear Data sẽ xóa dữ liệu local, vì vậy chỉ thực hiện sau khi đã xuất JSON.
