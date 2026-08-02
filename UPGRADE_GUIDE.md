# Cập nhật Good Daily lên v1.4.0

## Cách an toàn nhất

1. Mở bản Good Daily hiện tại.
2. Vào `Cá nhân → Xuất JSON` để tạo một file dự phòng.
3. Không gỡ cài đặt ứng dụng trên máy ảo/điện thoại.
4. Không chọn Clear Data.
5. Giải nén `GoodDaily_v1.4.0_music_notifications_ui.zip`.
6. Mở đúng thư mục có trực tiếp:

```text
app
gradle
build.gradle
settings.gradle
gradlew
gradlew.bat
```

7. Trong Android Studio chạy:

```text
Build → Clean Project
Build → Rebuild Project
```

8. Chạy app trên đúng thiết bị trước đó.

## Dữ liệu có được giữ không?

Có. Bản này giữ nguyên:

```text
applicationId: com.sangapp.gooddaily
Room database: good_daily_database
Room version: 3
```

Bài hát, âm báo, avatar và tùy chọn giao diện được lưu trong SharedPreferences. File âm thanh/ảnh được chọn bằng URI có quyền đọc lâu dài từ Android.

## Đưa code mới vào thư mục GitHub chính

Giữ một thư mục Git chính, sau đó chép code mới vào nhưng không đè `.git`, `.gradle`, `.idea`, `build` và `local.properties`.

Ví dụ PowerShell:

```powershell
robocopy `
"C:\Users\ADMIN\Downloads\GoodDaily_v1.4.0" `
"C:\Users\ADMIN\Documents\GoodDaily" `
/E /XD .git .gradle .idea build /XF local.properties

cd C:\Users\ADMIN\Documents\GoodDaily
git status
git add .
git commit -m "Update Good Daily v1.4.0 music and notification sounds"
git push
```
