# Good Daily v1.3.1

Bản vá lỗi biên dịch trong `BackupManager.java`: biến danh sách học tập dùng trong lambda Room transaction nay được khai báo final/effectively final.

# Good Daily 1.3.0

Good Daily là ứng dụng quản lý cá nhân chạy hoàn toàn offline trên Android.

- Package: `com.sangapp.gooddaily`
- Database: `good_daily_database`
- Java 17, XML, MVVM, Room/SQLite, WorkManager
- Không cần server để nhập, lưu và xem dữ liệu

## Điểm mới của bản 1.3.0

### Kế hoạch theo ngày và lịch Dương – Âm

- Lịch tuần vuốt ngang, hiển thị ngày Dương và ngày Âm.
- Chuyển tuần trước, tuần sau, quay về hôm nay hoặc mở lịch tháng để chọn ngày.
- Các ngày có thời gian biểu được đánh dấu bằng chấm trạng thái.
- Toàn bộ công việc, thời gian biểu, tiến độ học, thói quen và nhật ký thay đổi theo ngày đang chọn.

### Thời gian biểu theo khung giờ

- Thêm và sửa hoạt động với giờ bắt đầu, giờ kết thúc.
- Hỗ trợ khung giờ qua đêm, ví dụ ngủ từ 23:00 đến 07:00 hôm sau.
- Phân loại màu: học tập, làm việc/chạy xe, ngủ, tập luyện, nghỉ ngơi và khác.
- Có ghi chú và tùy chọn nhắc trước 15 phút bằng thông báo local.
- Chạm để sửa, giữ để xóa.

### Trạm học tập

- Từ mới được nhập bằng **số lượng**, không bắt buộc nhập từng từ và nghĩa.
- Nhập điểm thi thử của từng ngày.
- Đặt mục tiêu số từ mới trong tuần và xem thanh tiến độ.
- Có Pomodoro 25 phút; khi hoàn thành tự cộng vào thời gian học.
- Có thể ghi thêm các phiên học thủ công và xem tổng số phút đã học.

### Thói quen và nhật ký

- Thói quen dạng nút lớn, hoàn thành sẽ đổi màu và hiện dấu tích.
- Hiển thị chuỗi ngày liên tiếp của từng thói quen.
- Nhật ký theo từng ngày có nội dung, tag và tâm trạng.
- Lịch sử ghi chú hiển thị cả ngày Dương và ngày Âm.

### Tài chính hiện có

- Nhập hoặc điều chỉnh trực tiếp số tiền đang có hiện tại cho tiền mặt, ngân hàng và ví điện tử.
- Good Daily tự cân chỉnh số dư nền để tổng tiền hiện có khớp số tiền thực tế mà vẫn giữ lịch sử giao dịch.
- Hiển thị số dư riêng từng nguồn tiền.
- Thống kê và lọc lịch sử theo ngày, tháng hoặc năm bất kỳ.
- Hiển thị thu, chi và số tiền còn lại trong kỳ.
- Có thống kê nhanh tuần này, tháng này và năm nay.
- Chạm giao dịch để sửa, giữ để xóa.
- Form giao dịch là Material Bottom Sheet mở rộng toàn màn hình và có thể cuộn/kéo.
- Toàn bộ màn hình Tài chính cuộn liền mạch, danh sách giao dịch không còn bị cố định riêng.
- Giữ ngân sách tháng, cảnh báo tài chính và xuất PDF.

### Điều hướng và Tổng quan

- Sửa lỗi bấm **Tổng quan** nhưng phải bấm Back mới quay lại.
- Bottom Navigation điều hướng trực tiếp giữa năm màn hình chính.
- Các thẻ trên Tổng quan mở đúng khu vực chi tiết: số dư, lịch sử giao dịch, bữa ăn, số đo, học tập, thói quen và công việc.

### Cá nhân và giao diện

- Chọn ảnh đại diện từ ảnh trên điện thoại.
- Lưu quyền đọc ảnh để ảnh đại diện còn hiển thị sau khi mở lại app.
- Tiếp tục hỗ trợ bốn màu nhấn: măng tây, xanh dương, cam ấm và tím mận.
- Icon được gán màu/tint rõ ràng để tránh hiển thị thành khối đen.

### Dữ liệu

- Room Database nâng lên version 3.
- Migration `2 -> 3` tạo tài khoản tài chính, thời gian biểu, tiến độ học theo ngày và bổ sung tag/tâm trạng cho nhật ký.
- Giữ nguyên dữ liệu từ các bản cũ khi cập nhật đúng cách.
- Backup JSON version 3 chứa thêm tài khoản tài chính, thời gian biểu, số từ mới, điểm thi thử và mục tiêu từ mới tuần.

## Cách chạy

1. Giải nén ZIP.
2. Trong Android Studio chọn **File > Open**.
3. Mở thư mục có trực tiếp `app`, `gradle`, `build.gradle`, `settings.gradle`.
4. Chờ Gradle Sync hoàn tất.
5. Chọn máy ảo hoặc điện thoại rồi nhấn Run.

## Cập nhật mà không mất dữ liệu

1. Từ bản đang chạy, vào **Cá nhân > Xuất dữ liệu** để sao lưu JSON.
2. Không gỡ app và không bấm Clear Data.
3. Mở project 1.3.0 và Run trên cùng máy ảo/điện thoại.
4. Android sẽ cập nhật vì package vẫn là `com.sangapp.gooddaily`.
5. Room Migration sẽ giữ dữ liệu cũ.

## Phạm vi hiện tại

Bản 1.3.0 hoàn thiện các yêu cầu chính về lịch, thời gian biểu, số từ mới dạng số lượng, tài chính hiện có, lịch sử theo kỳ, avatar và điều hướng. Các module rất lớn như quản lý nợ đầy đủ, ca chạy xe chuyên sâu, nước/giấc ngủ/thuốc, mã hóa toàn bộ database và đồng bộ đám mây vẫn là các giai đoạn mở rộng riêng.
