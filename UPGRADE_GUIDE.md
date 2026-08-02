# Cập nhật lên Good Daily v2.4.0

1. Trong bản cũ, tạo một bản backup trước khi cập nhật.
2. Không gỡ ứng dụng và không bấm **Clear Data**.
3. Giải nén project v2.4.0.
4. Mở thư mục chứa trực tiếp `app`, `gradle`, `build.gradle`, `settings.gradle`.
5. Chọn **Build → Clean Project** rồi **Build → Rebuild Project**.
6. Run trên đúng máy ảo hoặc điện thoại đang dùng.

Bản này giữ nguyên:

- Package: `com.sangapp.gooddaily`
- Database: `good_daily_database`
- Room version: `6`

Vì không đổi schema Room, dữ liệu v2.3.x được giữ nguyên.
