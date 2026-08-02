# Changelog v2.4.0

## Điều hướng và khám phá chức năng

- Đưa toàn bộ công cụ tài chính nâng cao vào màn hình Tài chính.
- Đưa toàn bộ theo dõi sức khỏe nâng cao vào màn hình Sức khỏe.
- Chia công cụ Kế hoạch thành ba nhóm: Lịch & công việc, Học tập & thói quen, Nhật ký & nghiệm lý.
- Đưa hệ thống, backup, bảo mật, nhạc và tìm kiếm vào màn hình Cá nhân.
- Thêm lối tắt trực tiếp trên Tổng quan.
- Đổi “Trung tâm quản lý” thành “Tất cả chức năng”, chỉ dùng làm danh mục dự phòng.

## UI/UX

- Card công cụ dùng chung thiết kế Material 3, hai cột, icon rõ ràng và mô tả ngắn.
- Màu card tự thích nghi với theme Good Daily, Material You và Dark Mode.
- Mỗi card có content description để hỗ trợ TalkBack.
- Tăng padding cuối màn hình Sức khỏe để không bị Bottom Navigation che.

## Kiến trúc và kiểm thử

- Thêm `ModuleToolsRenderer`.
- Thêm `FeatureNavigator`.
- Thêm `FeaturePlacementCatalog`.
- Thêm `FeaturePlacementCatalogTest`: kiểm tra đủ 27 feature trong catalog đều có module sở hữu.

## Tương thích dữ liệu

- Room giữ nguyên version 6.
- Không cần migration mới.
- Giữ package `com.sangapp.gooddaily`.
