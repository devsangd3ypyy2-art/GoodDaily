# Kiến trúc Good Daily v2.2.0

## Mục tiêu

- Tránh package tổng hợp kiểu `ui/advanced`.
- Mỗi tính năng có ranh giới rõ giữa UI, nghiệp vụ và dữ liệu.
- Những module lớn mới không gọi DAO trực tiếp từ Fragment.
- Có thể kiểm thử calculator, repository contract, ViewModel và DAO độc lập.

## Cấu trúc chính

```text
com.sangapp.gooddaily
├── data/
│   ├── backup/
│   └── local/
│       ├── dao/
│       ├── entity/
│       └── prefs/
├── feature/
│   ├── driver/
│   │   ├── data/
│   │   │   ├── DriverDao
│   │   │   ├── DriverDataSource
│   │   │   ├── DriverRepository
│   │   │   ├── DriverSessionStore
│   │   │   └── entity/
│   │   ├── domain/
│   │   │   ├── ShiftProfitCalculator
│   │   │   ├── BatteryHealthCalculator
│   │   │   └── MaintenanceDueCalculator
│   │   └── ui/
│   │       ├── DriverDashboardFragment
│   │       └── DriverViewModel
│   └── metaphysics/
│       ├── data/
│       │   ├── DivinationDao
│       │   ├── DivinationSessionEntity
│       │   ├── MetaphysicsDataSource
│       │   └── MetaphysicsRepository
│       ├── domain/
│       │   ├── Trigram
│       │   ├── FiveElement
│       │   ├── HexagramCatalog
│       │   ├── MaiHoaCalculator
│       │   ├── AdvancedMaiHoaInterpreter
│       │   ├── LiuHaoCalculator
│       │   ├── EightPalaceAnalyzer
│       │   ├── NaJiaAnalyzer
│       │   ├── QuestionTopic
│       │   ├── HeavenlyStem / EarthlyBranch
│       │   └── SixRelative / SixSpirit
│       └── ui/
│           ├── MetaphysicsHomeFragment
│           ├── MetaphysicsViewModel
│           ├── maihoa/
│           ├── liuhao/
│           └── history/
├── ui/
│   ├── backup/
│   ├── finance/
│   ├── habit/
│   ├── health/
│   ├── planner/
│   ├── search/
│   ├── security/
│   └── featurehub/
└── util/
```

## Luồng dữ liệu module mới

```text
Fragment
   ↓ thao tác / quan sát LiveData
ViewModel
   ↓ interface
DataSource
   ↓ implementation
Repository
   ↓
Room DAO
   ↓
SQLite local
```

Domain calculator không phụ thuộc Android hoặc Room, nên có thể chạy unit test bằng JVM thường.

## Quy tắc package

- `feature/<name>/data`: Entity, DAO, data-source contract và repository.
- `feature/<name>/domain`: Công thức, mô hình và logic không phụ thuộc UI.
- `feature/<name>/ui`: Fragment, adapter và ViewModel của riêng tính năng.
- `ui/<domain>/widget`: custom view chỉ dùng trong một miền.
- `ui/widget`: chỉ giữ widget dùng chung toàn app.
- Không tạo lại một package `advanced`, `misc`, `other` hoặc `common` để chứa code không rõ chủ sở hữu.

## Database

`GoodDailyDatabase` vẫn là database duy nhất để bảo đảm backup, migration và transaction thống nhất. Các DAO mới được đặt bên trong feature nhưng được khai báo tại database gốc:

```java
public abstract DivinationDao divinationDao();
public abstract DriverDao driverDao();
```

## Hướng mở rộng

Nếu tiếp tục bổ sung Phục thần, Tam hợp cục và nhiều trường phái ứng kỳ, nên tách engine hiện tại thành:

```text
feature/metaphysics/domain/maihoa/
feature/metaphysics/domain/liuhao/
feature/metaphysics/domain/liuhao/najia/
feature/metaphysics/domain/liuhao/timing/
```

Khi bổ sung nhiều màn hình ca chạy, tách:

```text
feature/driver/ui/dashboard/
feature/driver/ui/shift/
feature/driver/ui/vehicle/
feature/driver/ui/maintenance/
```

Không đưa các màn hình này trở lại `ui/advanced`.
