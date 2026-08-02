# Good Daily v2.2.0

Good Daily là ứng dụng quản lý cá nhân Android chạy hoàn toàn local. Bản v2.2.0 nâng cấp riêng module **Mai Hoa Dịch Số và Lục Hào** từ mức lập quẻ cơ bản lên bộ diễn giải quy tắc chuyên sâu hơn.

## Thông tin project

- Package: `com.sangapp.gooddaily`
- Database: `good_daily_database`
- Room version: `6`
- Version code: `23`
- Version name: `2.2.0`
- Java 17, XML, Material Design 3, MVVM, Room/SQLite, Navigation, WorkManager và AlarmManager

## 1. Mai Hoa Dịch Số chuyên sâu

- Lập quẻ bằng ba số hoặc ngày giờ hiện tại.
- Tính quẻ chủ, quẻ hỗ, quẻ biến và hào động.
- Xác định Thể quái, Dụng quái và Dụng sau biến.
- Phân tích sinh–khắc giữa Thể và Dụng.
- Đánh giá khí mùa gần đúng của Thể và Dụng.
- Luận riêng theo chủ đề:
  - Công việc.
  - Tài chính.
  - Tình cảm.
  - Sức khỏe.
  - Học tập.
  - Xuất hành.
  - Giấy tờ/tranh chấp.
  - Tìm đồ thất lạc.
  - Hỏi thời điểm.
- Phân tích tượng của thượng quái, hạ quái và quẻ hỗ.
- Diễn giải ý nghĩa vị trí hào 1–6.
- Sinh cửa sổ thời gian tham khảo và mức độ nhất quán.
- Lưu toàn bộ kết luận, chi tiết kỹ thuật và thời gian vào lịch sử nghiệm lý.

## 2. Lục Hào Nạp Giáp chuyên sâu

- Gieo ba đồng xu sáu lần hoặc nhập tay `6`, `7`, `8`, `9`.
- Xác định quẻ chủ, quẻ hỗ, quẻ biến và hào động.
- Tự xác định Bát cung, đời quẻ, hào Thế và hào Ứng.
- Nạp Thiên Can và Địa Chi cho từng hào.
- Xác định Ngũ hành từng hào.
- Xác định Lục Thân theo hành của cung quẻ.
- An Lục Thần theo ngày can.
- Nhập/chỉnh Nguyệt kiến, ngày can và ngày chi.
- Đánh giá vượng suy theo Nhật–Nguyệt.
- Nhận diện:
  - Không Vong.
  - Nguyệt phá.
  - Nhật xung, Nhật hợp.
  - Hóa hợp, hóa xung.
  - Hóa hồi đầu sinh/khắc.
- Chọn Dụng thần theo chủ đề câu hỏi.
- Phân tích quan hệ Thế–Ứng.
- Sinh bảng Nạp Giáp sáu hào đầy đủ.
- Luận riêng cho tài chính, công việc, thi cử, tình cảm, sức khỏe, xuất hành, tranh chấp và tìm đồ.
- Sinh cửa sổ ứng kỳ tham khảo theo động/tĩnh, Không Vong, hợp/xung và địa chi Dụng thần.
- Hiển thị mức độ nhất quán cao/trung bình/thấp.

## 3. Lưu ý về lịch Can Chi

App tự điền gần đúng Nguyệt kiến và ngày Can Chi hiện tại, đồng thời cho phép chỉnh tay. Trong thực hành chuyên sâu, ranh giới tháng khí phụ thuộc tiết khí và trường phái sử dụng, vì vậy người dùng nên đối chiếu với lịch Can Chi mình tin dùng.

## 4. Phạm vi an toàn

- Đây là engine quy tắc văn hóa và công cụ nghiệm lý cá nhân.
- Không coi kết quả là dự báo chắc chắn.
- Không dùng thay tư vấn y tế, pháp lý, tài chính hoặc quyết định an toàn.
- Lục Hào v2.2 đã có Nạp Giáp, Bát cung, Thế–Ứng, Lục Thân, Lục Thần, Nhật–Nguyệt và Dụng thần theo chủ đề, nhưng chưa thay thế kinh nghiệm biện quẻ của người thực hành lâu năm.

## 5. Cấu trúc module

```text
feature/metaphysics/
├── data/
├── domain/
│   ├── AdvancedMaiHoaInterpreter.java
│   ├── EightPalaceAnalyzer.java
│   ├── NaJiaAnalyzer.java
│   ├── QuestionTopic.java
│   ├── EarthlyBranch.java
│   ├── HeavenlyStem.java
│   ├── SixRelative.java
│   └── SixSpirit.java
└── ui/
    ├── maihoa/
    ├── liuhao/
    └── history/
```

## 6. Kiểm thử

Bổ sung:

- `EightPalaceAnalyzerTest`
- `NaJiaAnalyzerTest`
- `AdvancedMaiHoaInterpreterTest`

Các lớp domain thuần Java đã được compile và chạy smoke test. Toàn bộ XML đã được kiểm tra cú pháp.

## 7. Cách chạy

1. Giải nén ZIP.
2. Mở thư mục chứa trực tiếp `app`, `gradle`, `build.gradle`, `settings.gradle` và `gradlew`.
3. Dùng Gradle JDK 17.
4. Chờ Gradle Sync.
5. Chọn **Build → Clean Project**.
6. Chọn **Build → Rebuild Project**.
7. Nhấn **Run**.

## 8. Cập nhật không mất dữ liệu

- Không gỡ ứng dụng cũ.
- Không bấm **Clear Data**.
- Nên backup GDZ/ZIP trước khi cập nhật.
- Package, database và Room version không đổi so với v2.1.0, nên không cần migration mới.

## 9. Giới hạn kiểm tra build

Môi trường đóng gói không có Android SDK và không truy cập được máy chủ Gradle, nên chưa chạy được `assembleDebug`. Việc compile các lớp domain, smoke test thuật toán và kiểm tra XML đã hoàn tất; Android Studio trên máy bạn là bước build cuối cùng.
