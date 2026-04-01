# UET Parking Slot Booking - Android Project

## 📋 Mô tả
Ứng dụng quản lý bãi đỗ xe cho các giảng đường của trường Đại học Công nghệ - ĐHQG Hà Nội.

## 🏗️ Cấu trúc Project

```
uet-parking-slot-booking/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/com/uet/parking/
│   │   │   │   ├── MainActivity.kt               # Activity chính
│   │   │   │   │
│   │   │   │   ├── ui/                           # Presentation Layer
│   │   │   │   │   ├── activity/                 # Activities
│   │   │   │   │   ├── screen/                   # Fragments/Screens
│   │   │   │   │   └── components/               # Custom Views & Components
│   │   │   │   │
│   │   │   │   ├── data/                         # Data Layer
│   │   │   │   │   ├── model/                    # Data Models (ParkingSlot, Lecture, etc.)
│   │   │   │   │   └── repository/               # Repository Pattern implementation
│   │   │   │   │
│   │   │   │   ├── domain/                       # Domain Layer (Business Logic)
│   │   │   │   │   └── usecase/                  # Use Cases (GetAvailableSlots, etc.)
│   │   │   │   │
│   │   │   │   ├── network/                      # Network Layer
│   │   │   │   │   └── api/                      # Retrofit interfaces & configuration
│   │   │   │   │
│   │   │   │   └── utils/                        # Utility classes
│   │   │   │       ├── Constants.kt              # Hằng số
│   │   │   │       ├── DateUtils.kt              # Xử lý ngày giờ
│   │   │   │       └── ToastUtils.kt             # Hiển thị Toast
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── layout/                       # XML Layouts
│   │   │   │   ├── drawable/                     # Drawable resources
│   │   │   │   ├── values/                       # Strings, colors, dimensions
│   │   │   │   └── mipmap/                       # App icons
│   │   │   │
│   │   │   └── AndroidManifest.xml               # Manifest file
│   │   │
│   │   ├── test/                                 # Unit tests
│   │   └── androidTest/                          # Instrumented tests
│   │
│   ├── build.gradle                              # App-level build config
│   └── proguard-rules.pro                        # Obfuscation rules
│
├── build.gradle                                  # Project-level build config
├── settings.gradle                               # Settings
└── README.md                                     # Tài liệu này
```

## 📁 Giải thích từng folder

| Folder | Chức năng | Ví dụ |
|--------|----------|------|
| **ui/activity** | Chứa các Activity của ứng dụng | MainActivity, ParkingListActivity |
| **ui/screen** | Chứa các Fragment/Screen | ParkingScreen, BookingScreen |
| **ui/components** | Custom Views và reusable components | ParkingSlotCard |
| **data/model** | Data models (POJO/Data classes) | ParkingSlot, Lecture |
| **data/repository** | Repository pattern - kết nối data sources | ParkingRepository |
| **domain/usecase** | Business logic layer | GetAvailableSlotsUseCase |
| **network/api** | Retrofit API interfaces | ParkingApiService, RetrofitClient |
| **utils** | Utility classes | Constants, DateUtils, ToastUtils |

## 🔧 Công nghệ sử dụng

- **Kotlin** - Ngôn ngữ lập trình chính
- **Android SDK** - API Android
- **Retrofit 2** - HTTP client cho API calls
- **Gson** - JSON serialization/deserialization
- **AndroidX** - Modern Android libraries
- **View Binding** - Type-safe view binding

## 🚀 Cách chạy dự án

### 1. **Clone/Import vào Android Studio**
   - File → Open → Chọn thư mục `uet-parking-slot-booking`

### 2. **Sync Gradle**
   - Android Studio sẽ tự động sync dependencies từ `build.gradle`

### 3. **Chọn emulator hoặc device**
   - Tools → AVD Manager (hoặc kết nối device vật lý)

### 4. **Chạy ứng dụng**
   - Run → Run 'app' (hoặc Shift + F10)

## 📱 HTML Layout

File `activity_main.xml` chứa giao diện chính:
- **Header**: Tiêu đề "Quản lý Bãi Đỗ Xe UET"
- **Statistics Card**: Hiển thị tổng chỗ, chỗ trống, chỗ đã đỗ
- **Check Availability Button**: Nút kiểm tra chỗ trống
- **Responsive Design**: Thích ứng với các kích thước màn hình khác nhau

## 💾 Dữ liệu trong Repository

Dữ liệu được lưu trữ dưới dạng mock (giả lập) trong `ParkingRepository`:

```kotlin
// Mock data
- Slot A01, A02 (Khu A - Giảng đường 101)
- Slot B01, B02, B03 (Khu B - Giảng đường 201)

// Trạng thái: Available/Occupied
// Loại: Regular/Reserved
```

## 🔗 Cấu trúc luồng dữ liệu (Data Flow)

```
UI (MainActivity)
    ↓
Repository (getData)
    ↓ (mock data)
Model (ParkingSlot)
    ↓
UI (hiển thị kết quả)
```

**Khi có API:**
```
UI → UseCase → Repository → Retrofit API → Model → UI
```

## 📝 Mở rộng ứng dụng

### Thêm tính năng mới:
1. **Thêm API thực**:
   - Cập nhật `RetrofitClient.BASE_URL`
   - Implement API calls trong Repository

2. **Thêm ViewModel** (MVVM Pattern):
   - Tạo `ParkingViewModel` để quản lý state
   - Sử dụng LiveData/StateFlow cho reactive updates

3. **Thêm Database** (Room):
   - Thêm Room dependencies
   - Tạo Entity, DAO, Database

4. **Thêm Dependency Injection** (Hilt):
   - Setup Hilt module
   - Inject dependencies

### Thêm screen mới:
```
1. Tạo layout XML trong res/layout/
2. Tạo Fragment trong ui/screen/
3. Tạo Repository method trong data/repository/
4. Tạo UseCase trong domain/usecase/
5. Implement trong MainActivity hoặc Navigation
```

## 🧪 Testing

### Unit Tests (test/):
```kotlin
// Viết test cho business logic
class ParkingRepositoryTest { ... }
```

### Instrumented Tests (androidTest/):
```kotlin
// Viết test cho UI
class MainActivityTest { ... }
```

## 🛠️ Troubleshooting

| Vấn đề | Giải pháp |
|--------|----------|
| Build error | Câp nhật SDK, clear cache: Build → Clean Project |
| Layout không hiển thị | Kiểm tra view ID trong XML khớp với Kotlin |
| API error | Kiểm tra BASE_URL, network permission |
| Gradle sync fail | Delete .gradle folder, sync lại |

## 📞 Liên hệ / Support

- Tài liệu: [Android Developer Docs](https://developer.android.com/)
- Kotlin: [Kotlin Docs](https://kotlinlang.org/docs/)

---

**Ver 1.0** | 2024
