# Validation v2.2.0

Đã kiểm tra:

- Compile toàn bộ lớp thuần Java trong `feature/metaphysics/domain` bằng `javac` thành công.
- Smoke test Lục Hào theo chủ đề tài chính: sinh quẻ, Bát cung, Thế–Ứng, bảng Nạp Giáp, Dụng thần, ứng kỳ và độ nhất quán.
- Smoke test Mai Hoa theo chủ đề công việc: sinh kết luận, chi tiết Thể–Dụng, thời gian và độ nhất quán.
- `EightPalaceAnalyzer` nhận đủ 64 quẻ, không có quẻ không xác định.
- Kiểm tra cú pháp 110 file XML: không có lỗi parse.
- Giữ Room version 6 nên không phát sinh migration mới.

Chưa thể chạy:

- `assembleDebug` và `testDebugUnitTest`, vì môi trường không có Android SDK và không tải được Gradle từ mạng.

Bước xác nhận cuối:

```text
Build → Clean Project
Build → Rebuild Project
Run
```
