# Good Daily v2.0.1

Good Daily là ứng dụng quản lý cá nhân Android chạy **hoàn toàn local**. Ứng dụng không kết nối tài khoản tài chính, không yêu cầu OTP và không gửi dữ liệu lên máy chủ.

- Package: `com.sangapp.gooddaily`
- Database: `good_daily_database`
- Room version: `5`
- Java 17, XML, Material Design 3, MVVM, Room/SQLite, Navigation, WorkManager và AlarmManager

## Nguyên tắc phần tài chính

Phần tài chính chỉ là sổ ghi chép trên điện thoại:

- Người dùng tự tạo ví hoặc tài khoản tiền và tự đặt tên.
- Tự nhập số dư hiện tại.
- Không có tên ngân hàng cố định.
- Không liên kết dịch vụ tài chính bên ngoài.
- Không đọc SMS, OTP, mật khẩu hoặc ứng dụng tài chính khác.

## Trung tâm quản lý

Mở `Cá nhân → Trung tâm quản lý` để truy cập các nhóm nâng cao.

### 1. Tài chính

- Tạo, sửa và ẩn nhiều ví/tài khoản tiền local.
- Nhập số dư đang có.
- Chuyển tiền nội bộ; khoản chuyển không tính thành thu hoặc chi.
- Tạo, đổi tên và xóa danh mục thu/chi.
- Tìm kiếm và lọc giao dịch theo từ khóa, thời gian, tài khoản và danh mục.
- Thêm, sửa, xóa giao dịch; đính kèm ảnh hóa đơn ngay trong biểu mẫu giao dịch.
- Giao dịch định kỳ theo ngày, tuần, tháng hoặc năm; worker tự tạo giao dịch khi đến hạn.
- Ngân sách tổng và ngân sách theo danh mục; cảnh báo 70%, 90% và 100%.
- Mục tiêu tiết kiệm và tiến độ.
- Quản lý khoản nợ và lịch sử thanh toán.
- Biểu đồ tròn cơ cấu chi tiêu.
- So sánh kỳ hiện tại với kỳ trước và dự báo tiền còn lại cuối tháng.
- Thống kê ngày, tháng, năm.
- Xuất CSV và PDF.

### 2. Sức khỏe

- Cân nặng, chiều cao, BMI, mỡ cơ thể, khối cơ.
- Vòng eo, ngực, tay và đùi.
- Hồ sơ cơ thể, mục tiêu cân nặng.
- Tính BMR, TDEE và kcal mục tiêu.
- Bữa ăn có loại bữa, khối lượng, kcal, protein, carb, chất béo và ảnh.
- Danh sách thực phẩm thường dùng.
- Theo dõi nước, giấc ngủ, tâm trạng, bài tập và thuốc.
- Báo cáo tuần, tháng, năm.
- Biểu đồ cân nặng, dinh dưỡng và dữ liệu sức khỏe.
- Sửa, xóa và hoàn tác dữ liệu chính.

Tỷ lệ mỡ và khối cơ là dữ liệu nhập từ cân hoặc thiết bị đo; điện thoại không tự đo chính xác hai chỉ số này.

### 3. Lịch, thời gian biểu và công việc

- Lịch tuần Dương lịch và Âm lịch.
- Lịch tháng dạng lưới, có ngày âm và các chấm màu báo dữ liệu bận.
- Chọn ngày cũ hoặc ngày tương lai.
- Xem ngày, tuần và tháng.
- Thời gian biểu dạng timeline theo giờ, hỗ trợ lịch qua đêm.
- Sao chép lịch của một ngày sang ngày khác.
- Mẫu thời gian biểu; worker có thể tạo lịch ngày theo các thứ đã chọn.
- Sự kiện lịch, công việc dự án, deadline, độ ưu tiên, trạng thái và công việc con trong phần mô tả.
- Cảnh báo sự kiện/công việc đến hạn.
- Lịch âm và can chi cơ bản.

Giao diện native hiện chưa hỗ trợ kéo-thả trực tiếp khối lịch bằng ngón tay; thời gian được sửa qua biểu mẫu để tránh sai lệch giờ.

### 4. Học tập

- Danh sách môn học và mục tiêu học.
- Phiên học, tổng số phút/ngày/tuần.
- Số lượng từ mới dạng số, không bắt nhập từng từ và nghĩa.
- Điểm thi thử và mục tiêu cá nhân.
- Pomodoro 25 phút trong màn hình chính.
- Lưu lịch sử Pomodoro 25, 45 hoặc 60 phút trong Trung tâm quản lý.
- Báo cáo học tập thông qua tìm kiếm, CSV và dữ liệu theo ngày.
- Tài liệu/ảnh đính kèm cho môn học hoặc mục tiêu.
- Nhắc học bằng thời gian biểu và nhắc nhở nâng cao.

### 5. Thói quen và mục tiêu

- Check-in hằng ngày và streak.
- Heatmap 12 tuần kiểu GitHub.
- Chuỗi hiện tại và chuỗi dài nhất.
- Tỷ lệ hoàn thành tuần và tháng.
- Thói quen nâng cao: ngày thực hiện, số lần mục tiêu, tạm dừng và nhắc riêng.
- Mục tiêu cá nhân: cân nặng, tiết kiệm, từ mới, tập luyện và mục tiêu tùy chỉnh.
- Worker tự cập nhật một số mục tiêu từ dữ liệu thực tế dựa vào tên/tag của mục tiêu.

### 6. Nhật ký và nghiệm lý

- Ghi chú nhanh theo ngày trong Kế hoạch.
- Nhiều mục nhật ký trong cùng ngày.
- Tiêu đề, nội dung, tag, mức quan trọng, yêu thích và file/ảnh đính kèm.
- Tìm kiếm nhật ký.
- Khóa riêng module nhật ký bằng PIN.
- Xuất nhật ký CSV hoặc PDF.
- Module Lục Hào, Mai Hoa Dịch Số, nội dung luận giải, kết quả thực tế và mức độ chính xác.

### 7. Ca chạy và phương tiện

- Ca làm việc: doanh thu, tổng chi phí, số cuốc, thời gian và lợi nhuận ròng.
- Hiển thị thu nhập theo giờ và theo cuốc khi đủ dữ liệu.
- Phương tiện: tên xe, biển số, số km, giá mua và thời gian sử dụng.
- Nhật ký nhiên liệu: số tiền, số lít, số km và mức tiêu hao.
- Bảo dưỡng: thay nhớt, lốp, phanh, ắc quy, hạn theo ngày/km và cảnh báo đến hạn.
- Giá mua và số năm sử dụng có thể dùng để theo dõi khấu hao trong hồ sơ phương tiện.

### 8. Nhắc nhở và âm thanh

- Nhắc một lần, hằng ngày, hằng tuần, hằng tháng và hằng năm.
- Chọn nhiều ngày trong tuần bằng tag, ví dụ `T2,T4,T6`.
- Báo trước theo số phút.
- Âm thanh riêng cho từng nhắc nhở hoặc âm báo chung của app.
- Tùy chọn rung; ghi `không rung` trong tag để tắt rung.
- Nhóm thông báo: sức khỏe, học tập, tài chính, thói quen và chung.
- Nút `Nhắc lại 10 phút` và `Hoàn thành` ngay trên notification.
- AlarmManager cho nhắc nâng cao, WorkManager cho các tác vụ nền.
- Tự khôi phục lịch nhắc sau khi khởi động lại thiết bị hoặc cập nhật ứng dụng.
- Chọn và phát nhạc/audio trong máy ở màn hình Âm thanh & Nhạc.

### 9. Bảo mật

- Mật khẩu đăng nhập local.
- PIN 4–8 số.
- PIN hash được mã hóa AES/GCM bằng khóa Android Keystore.
- Mở khóa sinh trắc học khi thiết bị hỗ trợ.
- Tự khóa sau 1, 5, 15 hoặc 30 phút.
- Khóa riêng Tài chính và Nhật ký.
- `FLAG_SECURE` để ẩn nội dung nhạy cảm khỏi ảnh chụp và màn hình ứng dụng gần đây.

Giới hạn kỹ thuật: file Room/SQLite chưa được mã hóa toàn bộ bằng SQLCipher. Vì vậy không nên root máy hoặc chia sẻ trực tiếp thư mục dữ liệu ứng dụng.

### 10. Sao lưu và chuyển máy

- JSON tương thích với các bản cũ.
- Backup đầy đủ dạng ZIP/GDZ: database, preferences, avatar, hóa đơn, nhật ký và file đính kèm.
- Mã hóa backup bằng mật khẩu AES-GCM/PBKDF2.
- Xem trước thông tin backup.
- Chọn `Gộp dữ liệu` hoặc `Thay thế dữ liệu`.
- Bỏ qua dữ liệu trùng theo cơ chế đối chiếu tốt nhất có thể.
- Sao lưu tự động theo tuần hoặc tháng, giữ nhiều phiên bản.
- Chia sẻ file backup.

PIN/khóa Keystore không được chuyển sang máy khác vì khóa được ràng buộc với thiết bị. Sau khi khôi phục trên máy mới, hãy đặt PIN mới.

### 11. UI/UX

- Material Design 3.
- Sáng, tối hoặc theo hệ thống.
- Material You trên Android 12+.
- Bốn màu nhấn thủ công.
- Trang chủ có lời chào, tên người dùng, chuông và avatar.
- Avatar chọn từ máy.
- Empty state, Snackbar hoàn tác, tìm kiếm toàn app và biểu đồ custom.
- Bố cục tablet thông qua `values-sw600dp`.
- Bottom sheet giao dịch cuộn toàn màn hình.

## Cách chạy

1. Dùng JDK 17.
2. Mở thư mục gốc có trực tiếp `app`, `gradle`, `build.gradle`, `settings.gradle` và `gradlew`.
3. Chờ Gradle Sync.
4. Chọn `Build → Clean Project`, sau đó `Build → Rebuild Project`.
5. Chạy trên điện thoại hoặc máy ảo Android.

## Giữ dữ liệu khi cập nhật

- Không gỡ app cũ.
- Không bấm Clear Data.
- Package và tên database không đổi.
- Room có migration từ version 1 đến version 5.
- Nên tạo một backup trước khi nâng cấp.

## Kiểm thử trong gói source

Có unit test nền tảng cho ngày tháng, tiền tệ và danh mục tính năng. Source đã được kiểm tra tĩnh về XML, resource, import nội bộ và cú pháp Java. Môi trường tạo gói không có Android SDK/Gradle cache đầy đủ nên chưa thể build APK thực tế; Android Studio trên máy của bạn vẫn là bước kiểm tra build cuối cùng.


## Sửa lỗi v2.0.1

- Sửa trùng phương thức `empty(String)` trong `LocalAccountManagerFragment`.
- Sửa kiểu dữ liệu chiều cao tối thiểu trong `HabitHeatmapView`.
- Sửa kiểu dữ liệu chiều cao tối thiểu trong `HealthTrendView`.
