# Validation v2.4.0

Đã thực hiện trong môi trường đóng gói:

- Parse thành công toàn bộ 112 file XML.
- Kiểm tra không có ID trùng trong các layout.
- Kiểm tra cân bằng dấu ngoặc của các file Java đã sửa.
- Compile thành công các class Java thuần: `FeatureDefinition`, `FeatureCatalog`, `FeaturePlacementCatalog`.
- Chạy smoke test xác nhận đủ 27 feature đều có vị trí hiển thị.
- Kiểm tra các drawable mới sử dụng đều tồn tại.
- Kiểm tra các ID ViewBinding mới trùng khớp XML.

Chưa thực hiện được `assembleDebug` vì môi trường đóng gói không có Android SDK. Build cuối cần chạy trong Android Studio.
