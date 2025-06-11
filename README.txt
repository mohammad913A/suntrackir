# SunTrackir - اپلیکیشن نجومی

## هدف
ساخت اپلیکیشن نجومی با داشبورد خیره‌کننده، تقویم قمری انقلابی (شروع از رمضان)، و پشتیبانی آفلاین.

## نقشه راه
1. **فاز 1: داشبورد خیره‌کننده**
   - انیمیشن خورشید و ماه با Lottie.
   - قوس با گرگ‌ومیش فعلی (نجومی: خط سفید).
   - بک‌گراند گرادینت پویا.
   - تقویم ماهانه تعاملی.
   - گام‌شمار سال قمری.
2. **فاز 2: تقویم قمری**
   - شروع از 1 رمضان 1446 (1403/12/10).
   - کبیسه‌سازی: 355 روز.
   - محاسبات با Astronomy.kt.
3. **فاز 3: ویژگی‌های اضافی**
   - اعلان، ویجت، نقشه آسمان.
4. **فاز 4: انتشار**
   - تست و بهینه‌سازی.

## مسیر فایل‌ها
- `app/src/main/res/drawable/progress_sun.xml`: نوار پیشرفت خورشید.
- `app/src/main/res/drawable/progress_moon.xml`: نوار پیشرفت ماه.
- `app/src/main/res/raw/sun_animation.json`: انیمیشن خورشید.
- `app/src/main/res/raw/moon_animation.json`: انیمیشن ماه.
- `app/src/main/java/com/example/suntrackir/ui/MainDashboard.kt`: داشبورد.
- `app/src/main/java/com/example/suntrackir/viewmodel/SunMoonViewModel.kt`: ویومدل.
- `app/src/main/java/com/example/suntrackir/data/HijriCalendar.kt`: تقویم قمری.
- `app/src/main/java/com/example/suntrackir/data/Astronomy.kt`: محاسبات نجومی.

## وابستگی‌ها
```gradle
implementation "androidx.compose.ui:ui:1.6.7"
implementation "androidx.compose.material3:material3:1.2.1"
implementation "com.airbnb.android:lottie-compose:6.4.0"
implementation "com.github.msarhan:ummalqura-calendar:1.1.12"
implementation "androidx.room:room-runtime:2.6.1"
kapt "androidx.room:room-compiler:2.6.1"
implementation "androidx.room:room-ktx:2.6.1"
implementation "org.threeten:threetenbp:1.6.8"
```

## گفت‌وگوها
- [گفت‌وگوی اصلی: نقشه راه و فیچرها](#)
- [خطایابی و پیاده‌سازی SunTrackir](#)
- [طراحی و انیمیشن SunTrackir](#)

## وضعیت
- فاز 1: در حال پیاده‌سازی داشبورد.
- بعدی: ادغام Astronomy.kt و انتخاب انیمیشن‌های Lottie.