# Good Daily v1.3.2

Bản cập nhật sửa toàn bộ giao diện chế độ tối và bổ sung lựa chọn chế độ hiển thị.

## Thay đổi chính

- Thêm bộ màu riêng trong `values-night/colors.xml`.
- Nền tối và chữ sáng có độ tương phản rõ ràng.
- Card, dialog, bottom sheet, ô nhập, thanh điều hướng và lịch đều hỗ trợ dark mode.
- Màu thu, chi, cảnh báo và các loại thời gian biểu có phiên bản tối riêng.
- Không còn dùng các màu nền sáng viết cứng trong layout.
- Icon trên nền màu tự chọn màu đen hoặc trắng để dễ nhìn.
- Thanh trạng thái và thanh điều hướng hệ thống đổi icon sáng/tối phù hợp.
- Tắt Force Dark tự động để Android không tự biến đổi màu gây đen chữ hoặc đen icon.

## Chọn chế độ hiển thị

Vào:

`Cá nhân → Cài đặt và bảo mật → Chế độ hiển thị`

Có ba lựa chọn:

- Theo máy
- Sáng
- Tối

Lựa chọn được lưu trên điện thoại và giữ nguyên khi mở lại ứng dụng.

## Cập nhật không mất dữ liệu

Project vẫn dùng:

- Package: `com.sangapp.gooddaily`
- Database: `good_daily_database`
- Room version: 3

Không gỡ ứng dụng và không bấm Clear Data. Mở project mới, Sync Gradle rồi Run trên đúng máy ảo hoặc điện thoại cũ.
