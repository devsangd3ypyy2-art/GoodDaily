# Good Daily v2.3.1 – sửa chuông thông báo

## Lỗi đã xử lý

- Nút “Nghe thử” trước đây chỉ phát trực tiếp bằng `Ringtone`, không kiểm tra Notification Channel thật.
- File nhạc chọn qua trình chọn tài liệu được lưu bằng URI SAF; Android System UI có thể không đọc được URI đó khi app chạy nền.
- Notification Channel cũ có thể đã bị hệ thống đặt thành im lặng.

## Thay đổi

- Chép file nhạc được chọn vào `MediaStore/Notifications/GoodDaily` trên Android 10+.
- Tạo Notification Channel phiên bản mới có âm thanh và rung.
- Nút “Gửi thử” gửi notification thật để kiểm tra.
- Có nút mở thẳng phần cài đặt thông báo của Good Daily.
- Nếu file âm thanh không hợp lệ, tự quay về âm báo mặc định.

## Cách kiểm tra

1. Cá nhân → Âm thanh & nhạc.
2. Chọn nhạc chuông hệ thống hoặc chọn bài hát rồi bấm “Dùng làm âm báo”.
3. Bấm “Gửi thử”.
4. Nếu không nghe, bấm “Mở cài đặt thông báo của điện thoại” và bật Âm thanh cho các kênh Good Daily.
5. Tăng âm lượng Thông báo và tắt chế độ Không làm phiền.
