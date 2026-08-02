# Good Daily v1.4.0

Good Daily là ứng dụng quản lý cá nhân Android chạy cục bộ bằng Java, XML, MVVM, Room/SQLite và WorkManager.

## Điểm mới trong v1.4.0

### Trang Tổng quan mới

- Hiển thị lời chào theo thời gian: sáng, chiều hoặc tối kèm tên người dùng.
- Avatar nằm ở góc phải và mở nhanh trang Cá nhân.
- Nút chuông nằm cạnh avatar và mở Trình quản lý thông báo.
- Hiệu ứng fade-in nhẹ khi mở Tổng quan.
- Các thẻ Tổng quan vẫn có thể bấm để mở đúng module chi tiết.

### Trung tâm Âm thanh & Nhạc

Mở tại: `Cá nhân → Âm thanh & nhạc`.

- Chọn file nhạc/audio có trong điện thoại bằng Storage Access Framework.
- Không cần xin quyền đọc toàn bộ bộ nhớ.
- Phát, tạm dừng, tiếp tục và dừng bài hát ngay trong app.
- Ghi nhớ bài hát đã chọn sau khi đóng app.
- Dùng bài hát đang chọn làm âm báo của Good Daily.
- Chọn nhạc chuông thông báo có sẵn của hệ thống.
- Nghe thử âm báo.
- Khôi phục âm báo mặc định.

Âm báo tùy chỉnh được áp dụng cho:

- Nhắc tổng kết ngày.
- Nhắc nhở tùy chỉnh.
- Nhắc thời gian biểu.
- Cảnh báo tài chính.

Lưu ý: âm báo chỉ thay đổi thông báo của Good Daily, không thay đổi nhạc chuông cuộc gọi của điện thoại. Trình phát nhạc hiện hoạt động trong màn hình Âm thanh & Nhạc và dừng khi rời màn hình.

### Material You / Dynamic Colors

Mở tại: `Cá nhân → Cài đặt và bảo mật → Màu theo hình nền`.

- Android 12 trở lên có thể lấy bảng màu từ hình nền điện thoại.
- Khi bật Material You, bốn màu thủ công sẽ tạm khóa để tránh xung đột.
- Khi tắt, app quay lại bốn màu Good Daily: măng tây, xanh dương, cam ấm và tím mận.
- Tiếp tục hỗ trợ chế độ Theo máy, Sáng và Tối.

### Tài chính thủ công, không liên kết ngân hàng

- Good Daily không kết nối API ngân hàng hoặc ví điện tử.
- Tiền mặt, tiền trong tài khoản và ví điện tử đều do người dùng tự nhập.
- Không yêu cầu mật khẩu ngân hàng, OTP hoặc quyền truy cập tài khoản tài chính.

### Hệ thống thông báo

- Notification Channel được tạo theo âm thanh đang chọn.
- Khi đổi âm báo, các kênh cũ do Good Daily tạo sẽ được làm mới.
- Trên Android cũ hơn Android 8, âm thanh được gắn trực tiếp vào notification.

## Chức năng có sẵn từ các bản trước

- Đăng ký, đăng nhập, đăng xuất cục bộ.
- Thu chi, số tiền hiện có, ngân sách và thống kê ngày/tháng/năm.
- Sửa và xóa giao dịch.
- Sức khỏe, cân nặng, BMI, bữa ăn, kcal và protein.
- Lịch tuần, chọn ngày, thời gian biểu theo giờ và nhắc trước 15 phút.
- Pomodoro, tổng thời gian học, số từ mới dạng số lượng và điểm thi thử.
- Thói quen, streak và nhật ký theo ngày.
- Avatar tùy chỉnh.
- Nhắc nhở một lần, hằng ngày và hằng tuần.
- Sao lưu/khôi phục JSON và xuất báo cáo tài chính PDF.
- Giao diện sáng/tối và bốn màu nhấn.

## Công nghệ

- Java 17
- XML + Material Design 3
- MVVM
- Room / SQLite
- Navigation Component
- WorkManager
- MediaPlayer
- RingtoneManager
- Storage Access Framework
- ViewBinding

## Cách chạy

1. Mở thư mục project có trực tiếp `app`, `gradle`, `build.gradle` và `settings.gradle`.
2. Chờ Gradle Sync hoàn tất.
3. Chọn máy ảo hoặc điện thoại.
4. Nhấn Run.

## Cập nhật không mất dữ liệu

- Giữ nguyên package: `com.sangapp.gooddaily`.
- Giữ nguyên database: `good_daily_database`.
- Room vẫn ở version 3 vì bản này không thay đổi bảng dữ liệu.
- Không gỡ app cũ và không bấm Clear Data.

## Bản vá 1.4.1

- Sửa lỗi biên dịch `ThemeUtils.java: cannot find symbol variable colorPrimary`.
- Không còn phụ thuộc trực tiếp vào `com.google.android.material.R.attr.colorPrimary`.
- Vẫn giữ Material You, giao diện sáng/tối và bốn màu thủ công.
