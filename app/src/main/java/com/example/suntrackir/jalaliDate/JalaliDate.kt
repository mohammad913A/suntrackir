package com.example.suntrackir.jalaliDate

import org.threeten.bp.LocalDate

data class JalaliDate(val year: Int, val month: Int, val day: Int) {
    override fun toString(): String = "$year/${month.toString().padStart(2, '0')}/${day.toString().padStart(2, '0')}"

    companion object {
        // پیاده‌سازی ساده برای تبدیل تاریخ میلادی به شمسی
        fun fromGregorian(gregorian: LocalDate): JalaliDate {
            // این یه پیاده‌سازی ساده‌ست، برای دقت بیشتر از کتابخونه مثل PersianCalendar استفاده کن
            val year = gregorian.year + 621
            val month = gregorian.monthValue
            val day = gregorian.dayOfMonth
            return JalaliDate(year, month, day)
        }
    }
}