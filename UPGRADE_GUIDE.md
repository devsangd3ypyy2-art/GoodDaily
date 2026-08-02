# Cập nhật Good Daily v2.1.x lên v2.2.0

Bản v2.2.0 giữ nguyên:

- Package: `com.sangapp.gooddaily`
- Database: `good_daily_database`
- Room version: `6`

Vì không thay đổi schema Room, dữ liệu cũ được giữ nguyên khi cài đè.

## Cách cập nhật an toàn

1. Trong app cũ, tạo một bản sao lưu GDZ/ZIP.
2. Không gỡ ứng dụng cũ.
3. Không bấm **Clear Data**.
4. Giải nén `GoodDaily_v2.2.0_advanced_divination.zip`.
5. Mở thư mục chứa trực tiếp:

```text
app
gradle
build.gradle
settings.gradle
gradlew
```

6. Dùng Gradle JDK 17.
7. Chọn **Build → Clean Project**.
8. Chọn **Build → Rebuild Project**.
9. Run trên đúng máy ảo hoặc điện thoại đang cài bản cũ.

## Nếu dùng thư mục GitHub chính

Chép source mới vào thư mục Git chính nhưng giữ nguyên:

```text
.git
local.properties
```

Sau khi build thành công:

```powershell
git status
git add .
git commit -m "Update Good Daily v2.2.0 advanced Mai Hoa and Liu Hao"
git push
```

## Dữ liệu lịch sử quẻ

Các quẻ đã lưu ở v2.1 vẫn đọc được. Quẻ mới của v2.2 sẽ lưu thêm toàn bộ phần:

- Kết luận theo chủ đề.
- Bảng Nạp Giáp hoặc phân tích Thể–Dụng.
- Cửa sổ thời gian tham khảo.
- Mức độ nhất quán.

Không cần migration vì các phần này được lưu trong trường luận giải hiện có.
