# Good Daily v2.0.1 – Compile fixes

Đã sửa ba lỗi Java mà Android Studio báo:

1. `LocalAccountManagerFragment.java`
   - Đổi hàm tạo giao diện trạng thái trống từ `empty(String)` thành `emptyState(String)`.
   - Giữ hàm kiểm tra chuỗi rỗng là `empty(String)`.
   - Khắc phục lỗi trùng phương thức và lỗi `TextView cannot be converted to boolean`.

2. `HabitHeatmapView.java`
   - Đổi `setMinimumHeight(dp(170))` thành `setMinimumHeight(Math.round(dp(170)))`.

3. `HealthTrendView.java`
   - Đổi `setMinimumHeight(dp(220))` thành `setMinimumHeight(Math.round(dp(220)))`.

Version app được nâng lên `2.0.1` (`versionCode 21`).
