# Nâng cấp Good Daily lên v2.0.0

## Cách an toàn

1. Trong bản cũ, vào `Cá nhân → Dữ liệu` và xuất JSON hoặc backup dự phòng.
2. Không gỡ ứng dụng trên điện thoại/máy ảo.
3. Không bấm `Clear Data`.
4. Giải nén file ZIP v2.0.0.
5. Mở đúng thư mục có trực tiếp:

```text
app
gradle
build.gradle
settings.gradle
gradlew
gradlew.bat
```

6. Chọn `Build → Clean Project`.
7. Chọn `Build → Rebuild Project`.
8. Run trên đúng thiết bị đã cài bản cũ.

## Dữ liệu cũ

Bản v2 giữ nguyên:

```text
applicationId: com.sangapp.gooddaily
database: good_daily_database
```

Room nâng lên version 5 và có chuỗi migration:

```text
1 → 2 → 3 → 4 → 5
```

Các bảng mới phục vụ tài chính nâng cao, hồ sơ sức khỏe, mục tiêu, nhắc nhở, file đính kèm và dữ liệu quản lý tổng hợp.

## Đưa bản mới vào thư mục GitHub chính

Giữ một thư mục Git chính có `.git`, sau đó chép source mới vào nhưng không chép cache và cấu hình máy:

```powershell
robocopy `
"C:\Users\ADMIN\Downloads\GoodDaily_v2.0.0" `
"C:\Users\ADMIN\Documents\GoodDaily" `
/MIR /XD .git .gradle .idea build app\build /XF local.properties

cd C:\Users\ADMIN\Documents\GoodDaily
git status
git add .
git commit -m "Update Good Daily v2.0.0"
git push
```

Nếu bạn mở thẳng thư mục mới và muốn liên kết nó với repository hiện tại, hãy clone repository trước rồi chép source vào thư mục clone. Cách đó an toàn hơn việc tạo lịch sử Git mới.

## Khi build lỗi

Mở `Build Output`, mở rộng tác vụ lỗi và gửi dòng đầu tiên có dạng:

```text
path/to/File.java:123: error: ...
```

hoặc:

```text
path/to/layout.xml:45: error: ...
```

Không cần gửi riêng dòng `failed` vì dòng đó chưa cho biết nguyên nhân thật.
